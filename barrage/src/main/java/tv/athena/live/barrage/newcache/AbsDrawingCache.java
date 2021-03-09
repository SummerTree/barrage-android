package tv.athena.live.barrage.newcache;

import android.graphics.Bitmap;
import android.os.Build;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yaojun on 2019/3/12.
 */
public abstract class AbsDrawingCache<CONTENT> {

    private final CONTENT mContent;
    private final int mWidth;
    private final int mHeight;
    private final int mSize;
    /**
     * 被引用次数，openGL的并不需要，因为不会回收content
     */
    private AtomicInteger mReferenceCount = new AtomicInteger(0);

    private OnFreeCallback mCallback;


    AbsDrawingCache(Bitmap content) {
        mContent = createDrawingContent(content);
        mWidth = content.getWidth();
        mHeight = content.getHeight();
        mSize = getBitmapSize(content);
    }

    abstract CONTENT createDrawingContent(Bitmap raw);

    public CONTENT getContent() {
        return mContent;
    }

    public int getReferenceCount() {
        return mReferenceCount.get();
    }

    public void increaseReferenceCount() {
        mReferenceCount.incrementAndGet();
    }

    public void decreaseReferenceCount() {
        if (mReferenceCount.decrementAndGet() == 0 && mCallback != null) {
            mCallback.onFree();
        }
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getByteCount() {
        return mSize;
    }

    public abstract boolean isHoldingBitmap();

    public static class ViewDrawingCache extends AbsDrawingCache<Bitmap> {

        public ViewDrawingCache(Bitmap drawing) {
            super(drawing);
        }

        @Override
        Bitmap createDrawingContent(Bitmap raw) {
            return raw;
        }

        @Override
        public boolean isHoldingBitmap() {
            return true;
        }
    }

    public static class GLDrawingCache extends AbsDrawingCache<ByteBuffer> {

        public GLDrawingCache(Bitmap content) {
            super(content);
        }

        @Override
        ByteBuffer createDrawingContent(Bitmap raw) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(getBitmapSize(raw));
            buffer.clear();
            raw.copyPixelsToBuffer(buffer);
            buffer.position(0);

            return buffer;
        }

        @Override
        public boolean isHoldingBitmap() {
            return false;
        }
    }

    public void setCallback(OnFreeCallback callback) {
        mCallback = callback;
    }

    static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getAllocationByteCount();
        }
    }

    public interface OnFreeCallback {
        // mReferenceCount归0
        void onFree();
    }
}
