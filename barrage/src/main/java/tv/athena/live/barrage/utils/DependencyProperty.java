package tv.athena.live.barrage.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 依赖属性，保存特定类型的数据{@link T}，并通过绑定方法对外暴露数据
 *
 * @author qingfeng
 */
public class DependencyProperty<T> {
    private final Entity<T> mEntity;
    private final T mDefaultValue;
    private final Set<Observer<T>> mHandlers = new HashSet<>();
    private volatile T mValue;

    public DependencyProperty() {
        this(null);
    }

    public DependencyProperty(T defaultValue) {
        mValue = mDefaultValue = defaultValue;
        mEntity = new Entity<T>() {
            @Override
            public T get() {
                return DependencyProperty.this.get();
            }

            @Override
            public boolean isDefault() {
                return DependencyProperty.this.isDefault();
            }

            @Override
            public void bind(Observer<T> observer) {
                DependencyProperty.this.bind(observer);
            }

            @Override
            public void unbind(Observer<T> observer) {
                DependencyProperty.this.unbind(observer);
            }

            @Override
            public void subscribe(Observer<T> observer) {
                DependencyProperty.this.subscribe(observer);
            }

            @Override
            public void unsubscribe(Observer<T> observer) {
                DependencyProperty.this.unsubscribe(observer);
            }
        };
    }

    public void set(T value) {
        boolean needNotify = needNotify(value);
        mValue = value;
        if (needNotify) {
            notifyPropChange(value);
        }
    }

    public Entity<T> getEntity() {
        return mEntity;
    }

    public void reset() {
        set(mDefaultValue);
    }

    public void reNotify() {
        notifyPropChange(mValue);
    }

    public T get() {
        return mValue;
    }

    public boolean isDefault() {
        T value = mValue;
        return value == null ? mDefaultValue == null : value.equals(mDefaultValue);
    }

    @Override
    public String toString() {
        return String.format("Dp@%s[%s]", Integer.toHexString(hashCode()), String.valueOf(mValue));
    }

    protected boolean needNotify(T value) {
        return !Objects.equals(value, mValue);
    }

    private void notifyPropChange(final T value) {
        onPropChange(getCopyHandlers(), value);
    }

    private Observer<T>[] getCopyHandlers() {
        final Observer[] copyHandlers;
        synchronized (mHandlers) {
            copyHandlers = new Observer[mHandlers.size()];
            mHandlers.toArray(copyHandlers);
        }
        return copyHandlers;
    }


    private void onPropChange(Observer<T>[] copyHandlers, T value) {
        for (Observer<T> handler : copyHandlers) {
            handler.onDeliverPropChange(value);
        }
    }

    /**
     * 马上回调数据，并且在每次数据改变的时候返回数据
     *
     * @param observer 需要绑定到的观察者
     */
    public void bind(Observer<T> observer) {
        observer.onDeliverPropChange(get());
        subscribe(observer);
    }

    /**
     * 与{@link #subscribe(Observer)}一致，取消数据绑定
     */
    public void unbind(Observer<T> observer) {
        unsubscribe(observer);
    }

    /**
     * 在每次数据改变的时候返回数据（第一次不会返回数据，注意与{@link #bind(Observer)}的区别）
     */
    public void subscribe(Observer<T> observer) {
        synchronized (mHandlers) {
            mHandlers.add(observer);
        }
    }

    /**
     * 取消数据订阅，取消之后不会再收到数据改变的回调
     */
    public void unsubscribe(Observer<T> observer) {
        synchronized (mHandlers) {
            mHandlers.remove(observer);
        }
    }

    public boolean hasObservers() {
        boolean hasObservers = false;
        synchronized (mHandlers) {
            hasObservers = mHandlers.size() > 0;
        }
        return hasObservers;
    }

    public Observer<T>[] getObservers() {
        return getCopyHandlers();
    }

    // public interface/classes
    public static abstract class Observer<T> {
        private Handler mHandler;
        private boolean mPaused;
        private T mCache;
        private boolean mHasCache;

        /**
         * 返回分发消息的looper 如果looper为空，则会同步执行消息
         * <p>
         * 默认返回主线程looper
         * <p>
         * TODO: 默认返回null
         */
        public Looper getDeliverLooper() {
            return Looper.getMainLooper();
        }

        private Handler ensureDeliverHandler() {
            if (mHandler == null) {
                mHandler = new Handler(getDeliverLooper());
            }
            return mHandler;
        }

        /**
         * 暂时消息接收，如果暂停期间有数据变更，在调用{@link #resume()}之后会notify一次最新的数据
         */
        public synchronized void pause() {
            if (mPaused) {
                return;
            }
            mPaused = true;
        }

        public boolean isPaused() {
            return mPaused;
        }

        /**
         * 恢复接收数据，如果数据有变更，会通知最后一次变更的数据
         */
        public synchronized void resume() {
            if (!mPaused) {
                return;
            }
            mPaused = false;
            if (mHasCache) {
                onDeliverPropChange(mCache);
                mCache = null;
                mHasCache = false;
            }
        }

        private void onDeliverPropChange(final T value) {
            synchronized (this) {
                if (mPaused) {
                    mCache = value;
                    mHasCache = true;
                    return;
                }
            }
            Looper deliverLooper = getDeliverLooper();
            if (deliverLooper == null) {
                onPropChange(value);
            } else {
                ensureDeliverHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        onPropChange(value);
                    }
                });
            }
        }

        /**
         * 数据变更回调，该方法会根据{@link #getDeliverLooper()}来选择回调线程
         *
         * @param value 变更的数据
         */
        public abstract void onPropChange(T value);
    }

    public interface Entity<T> {

        T get();

        boolean isDefault();

        /**
         * 马上回调数据，并且在每次数据改变的时候返回数据
         *
         * @param observer 需要绑定到的观察者
         */
        void bind(Observer<T> observer);

        /**
         * 与{@link #subscribe(Observer)}一致，取消数据绑定
         */
        void unbind(Observer<T> observer);

        /**
         * 在每次数据改变的时候返回数据（第一次不会返回数据，注意与{@link #bind(Observer)}的区别）
         */
        void subscribe(Observer<T> observer);

        /**
         * 取消数据订阅，取消之后不会再收到数据改变的回调
         */
        void unsubscribe(Observer<T> observer);
    }
}
