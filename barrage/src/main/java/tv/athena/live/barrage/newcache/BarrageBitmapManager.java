package tv.athena.live.barrage.newcache;

import android.graphics.Bitmap;
import android.os.Build;

import tv.athena.live.barrage.config.BarrageLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yaojun on 2019/3/12.
 */
public class BarrageBitmapManager {

    private static final Bitmap.Config RGB_CONFIG = Bitmap.Config.ARGB_4444;

    /**
     * 按bitmap从小到大排序的回收池
     */
    private List<BitmapCache> mRefPool = new ArrayList<>();

    /**
     * 回收Bitmap，加到回收池
     */
    public void recycle(Bitmap bitmap) {
        final BitmapCache newCache = new BitmapCache(bitmap);
        mRefPool.add(newCache);
    }

    public Bitmap get(int w, int h) {
        Bitmap result = null;

        for (BitmapCache cache : mRefPool) {
            final Bitmap bitmap = cache.mBitmapSoftReference.get();
            if (bitmap == null) {
                BarrageLog.debug("BarrageBitmapManager", "BarrageBitmapManager.get free pool, size is " + mRefPool.size());
                mRefPool.clear();
                break;
            }

            if (bitmap.getWidth() >= w
                    && bitmap.getWidth() < w * 1.3
                    && bitmap.getWidth() >= h) {
                result = bitmap;
                mRefPool.remove(cache);
                break;
            }
        }
        if (result != null) {
            BarrageLog.debug("BarrageBitmapManager", "BarrageBitmapManager.get, match! ");
            return result;
        }
        return createBitmap(w, h);
    }

    private Bitmap createBitmap(int w, int h) {
        return Bitmap.createBitmap(w, h, RGB_CONFIG);
    }

    public static class BitmapCache {
        final WeakReference<Bitmap> mBitmapSoftReference;
        /**
         * 4.4以上存AllocationByteCount，以下存ByteCount
         */
        final int mByteCount;

        BitmapCache(Bitmap bitmap) {
            mBitmapSoftReference = new WeakReference<>(bitmap);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mByteCount = bitmap.getAllocationByteCount();
            } else {
                mByteCount = bitmap.getByteCount();
            }
        }
    }
}
