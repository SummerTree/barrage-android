package tv.athena.live.barrage.newcache;

import android.graphics.Bitmap;
import android.util.LruCache;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author yaojun on 2019/3/12.
 */
public abstract class LruDrawingCacheManager<CONTENT> extends AbsDrawingCacheManager<CONTENT> {

    public static final int DEFAULT_MAX_CACHE_SIZE = 50;

    private final LruCache<Integer, AbsDrawingCache<CONTENT>> mCache;

    public LruDrawingCacheManager(BarrageBitmapManager bitmapManager, final DrawingCacheStrategy strategy) {
        super(bitmapManager);
        mCache = new LruCache<Integer, AbsDrawingCache<CONTENT>>(strategy.maxSize()) {
            @Override
            protected void entryRemoved(boolean evicted, Integer key, AbsDrawingCache<CONTENT> oldValue, AbsDrawingCache<CONTENT> newValue) {
                if (oldValue.isHoldingBitmap() && oldValue.getReferenceCount() == 0) {
                    // 持有bitmap，切没有被引用了，释放入复用池
                    mBitmapManager.recycle((Bitmap) oldValue.getContent());
                }
            }

            @Override
            protected int sizeOf(Integer key, AbsDrawingCache<CONTENT> value) {
                return strategy.sizeOf(value);
            }
        };
    }

    private void dumpDebugInfo() {
        final Map<Integer, AbsDrawingCache<CONTENT>> snapshot = mCache.snapshot();
        int size = 0;
        for (AbsDrawingCache<CONTENT> cache : snapshot.values()) {
            size += cache.getByteCount();
        }
    }


    @Override
    public void add2Cache(int characteristic, AbsDrawingCache<CONTENT> cache) {
        mCache.put(characteristic, cache);
    }

    @Override
    public AbsDrawingCache<CONTENT> getCache(int characteristic) {
        final AbsDrawingCache<CONTENT> cache = mCache.get(characteristic);
        if (cache == null) {
            return null;
        }
        return cache;
    }

    @NotNull
    private static DrawingCacheStrategy getDefaultCacheStrategy() {
        return new DrawingCacheStrategy() {
            @Override
            public int sizeOf(AbsDrawingCache value) {
                return 1;
            }

            @Override
            public int maxSize() {
                return DEFAULT_MAX_CACHE_SIZE;
            }
        };
    }

    public interface DrawingCacheStrategy {
        int sizeOf(AbsDrawingCache value);

        int maxSize();
    }
}
