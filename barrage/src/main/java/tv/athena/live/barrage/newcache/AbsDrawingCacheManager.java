package tv.athena.live.barrage.newcache;

/**
 * @author yaojun on 2019/3/12.
 */
public abstract class AbsDrawingCacheManager<CONTENT> {

    protected final BarrageBitmapManager mBitmapManager;

    public AbsDrawingCacheManager(BarrageBitmapManager bitmapManager) {
        mBitmapManager = bitmapManager;
    }

    public abstract void add2Cache(int characteristic, AbsDrawingCache<CONTENT> cache);

    public abstract AbsDrawingCache<CONTENT> getCache(int characteristic);

    public abstract void removeCache(int characteristic);
}
