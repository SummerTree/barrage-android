package tv.athena.live.barrage.utils.pool;

/**
 * Created by donghao on 2018/3/12.
 *
 * 采用借还缓存池的抽象工厂，自定义自己的对象的工厂需要继承实现这个
 */

public abstract class AbsPoolFactory<T> {

    /*** 默认缓冲池大小 ***/
    private static final int DEFAULT_POOL_SIZE = 10;

    protected static final String TAG = AbsPoolFactory.class.getSimpleName();

    private BorrowPools.IPool sPool;

    public AbsPoolFactory(){
        sPool = new BorrowPools.SynchronizedObjectPool(DEFAULT_POOL_SIZE);
    }

    /**
     * 自定义缓存池大小
     * @param customPoolSize
     */
    public AbsPoolFactory(int customPoolSize){
        sPool = new BorrowPools.SynchronizedObjectPool(customPoolSize);
    }

    /**
     * 获取缓存对象
     * @return
     */
    public T obtain() {
        T instance = (T)sPool.borrow();
        return (instance != null) ? instance : createObject();
    }

    /**
     * 回收缓存对象到池子中
     * @param obj
     */
    public void recycle(T obj) {
        resetObject(obj);
        sPool.revert(obj);
    }

    protected abstract T createObject();

    /**
     * 重置对象
     * @param obj
     */
    protected abstract void resetObject(T obj);
}
