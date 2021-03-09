package tv.athena.live.barrage.newcache;

import android.graphics.Bitmap;
import android.util.SparseArray;

/**
 * @author yaojun on 2019/4/19.
 */
public class SimpleDrawingCacheManager<CONTENT> extends AbsDrawingCacheManager<CONTENT> {

    private final SparseArray<AbsDrawingCache<CONTENT>> mCache;

    public SimpleDrawingCacheManager(BarrageBitmapManager bitmapManager) {
        super(bitmapManager);
        mCache = new SparseArray<>();
    }

    @Override
    public synchronized void add2Cache(final int characteristic, final AbsDrawingCache<CONTENT> cache) {
        mCache.put(characteristic, cache);
        cache.setCallback(new AbsDrawingCache.OnFreeCallback() {
            @Override
            public void onFree() {
                removeCache(characteristic);
                if (cache.isHoldingBitmap()) {
                    mBitmapManager.recycle((Bitmap) cache.getContent());
                }
            }
        });
    }

    @Override
    public synchronized AbsDrawingCache<CONTENT> getCache(int characteristic) {
        return mCache.get(characteristic);
    }

    @Override
    public synchronized void removeCache(int characteristic) {
        mCache.remove(characteristic);
    }
}
