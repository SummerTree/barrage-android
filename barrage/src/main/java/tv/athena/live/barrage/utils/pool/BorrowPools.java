package tv.athena.live.barrage.utils.pool;

import android.util.Log;

/**
 * Created by donghao on 2018/3/12.
 * 采用借还原理实现的对象池，用于简单的对象重复利用
 */

public class BorrowPools {

    private final static String TAG = ObjectPool.class.getSimpleName();

    public BorrowPools(){

    }

    /**
     *池对象接口
     * @param <T>
     */
    public interface IPool<T> {

        T borrow();

        boolean revert(T instance);

    }

    /**
     * 简单的对象池实现
     * @param <T>
     */
    public static class ObjectPool<T> implements IPool<T> {

        private final Object[] mPool;

        private int mPoolSize;

        public ObjectPool(int maxPoolSize) {
            if (maxPoolSize <= 0) {
                throw new RuntimeException("The max pool size must be > 0");
            }
            mPool = new Object[maxPoolSize];
        }

        @Override
        @SuppressWarnings("unchecked")
        public T borrow() {
            if (mPoolSize > 0) {
                final int lastPooledIndex = mPoolSize - 1;
                T instance = (T) mPool[lastPooledIndex];
                mPool[lastPooledIndex] = null;
                mPoolSize--;
                return instance;
            }
            return null;
        }

        @Override
        public boolean revert(T instance) {
            if (isInPool(instance)) {
                Log.e("pool", "already in the pool");
                throw new RuntimeException("Already in the pool!");
            }
            if (mPoolSize < mPool.length) {
                mPool[mPoolSize] = instance;
                mPoolSize++;
                return true;
            }
            return false;
        }

        private boolean isInPool(T instance) {
            for (int i = 0; i < mPoolSize; i++) {
                if (mPool[i] == instance) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 同步的对象池实现
     * @param <T>
     */
    public static class SynchronizedObjectPool<T> extends ObjectPool<T> {

        private Object mLock = new Object();

        public SynchronizedObjectPool(int maxPoolSize) {
            super(maxPoolSize);
        }

        @Override
        public T borrow(){
            synchronized (mLock){
                return super.borrow();
            }
        }

        @Override
        public boolean revert(T instance){
            synchronized (mLock){
                return super.revert(instance);
            }
        }

    }


}
