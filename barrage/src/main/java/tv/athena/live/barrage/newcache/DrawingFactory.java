package tv.athena.live.barrage.newcache;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.utils.DensityUtil;

import org.jetbrains.annotations.NotNull;

/**
 * @author yaojun on 2019/3/13.
 */
public class DrawingFactory<CONTENT> {

    private final Canvas mCanvas = new Canvas();
    private final TextPaint mTextPaint;
    private final int mCharHeight;
    private final int mCharWidth;

    private final BuildMachine<CONTENT> mBuildMachine;
    private final AbsDrawingCacheManager<CONTENT> mCacheManager;
    private final BarrageBitmapManager mBitmapManager = new BarrageBitmapManager();


    public DrawingFactory(TextPaint mTextPaint, int charWidth, int charHeight, BuildMachine<CONTENT> buildMachine) {
        this.mTextPaint = mTextPaint;
        this.mCharHeight = charHeight;
        this.mCharWidth = charWidth;
        this.mBuildMachine = buildMachine;
        this.mCacheManager = new SimpleDrawingCacheManager<>(mBitmapManager);
    }

    public AbsDrawingCache<CONTENT> draw(GunPowder powder) {
        final int characteristic = powder.getCharacteristic();
        final AbsDrawingCache<CONTENT> drawingCache = mCacheManager.getCache(characteristic);
        if (drawingCache != null) {
            return drawingCache;
        }

        final Bitmap bitmap = createNewDrawing(powder);
        final AbsDrawingCache<CONTENT> newDrawingCache = mBuildMachine.createDrawingCache(bitmap);
        mCacheManager.add2Cache(characteristic, newDrawingCache);
        if (!newDrawingCache.isHoldingBitmap()) {
            mBitmapManager.recycle(bitmap);
        }

        return newDrawingCache;
    }

    public BarrageBitmapManager getBitmapManager() {
        return mBitmapManager;
    }

    @NotNull
    private Bitmap createNewDrawing(GunPowder powder) {
        return GunPowder.DIRECTION_LTR == powder.mDirection ? horizontalBullet(powder)
                : verticalBullet(powder);

    }

    private Bitmap verticalBullet(GunPowder powder) {
        final int width = mCharWidth;
        int height = mCharHeight * powder.mPowder.length();
        height = height <= 0 ? 1 : height;
        float baseline = -mTextPaint.ascent() + 0.5f;

        final Bitmap bitmap = mBitmapManager.get(width, height);
        bitmap.eraseColor(0x00000000);
        mCanvas.setBitmap(bitmap);

        if (powder.mIsOwnBarrage) {
            drawBorder(mCanvas, mTextPaint, width, height);
        }
        mTextPaint.setColor(powder.mColor);
        for (int i = 0; i < powder.mPowder.length(); ++i) {
            mCanvas.drawText(powder.mPowder.substring(i, i + 1),
                    0, baseline + i * mCharHeight, mTextPaint);
        }
        return bitmap;
    }

    private Bitmap horizontalBullet(GunPowder powder) {
        int width = (int) mTextPaint.measureText(powder.mPowder) + 1;
        width = width <= 0 ? 1 : width;
        if (powder.mIsOwnBarrage) {
            width += DensityUtil.dip2px(BarrageContext.gContext, 3.5f);
        }
        final int height = mCharHeight;
        final Bitmap bitmap = mBitmapManager.get(width, height);
        bitmap.eraseColor(0x00000000);
        mCanvas.setBitmap(bitmap);

        if (powder.mIsOwnBarrage) {
            drawBorder(mCanvas, mTextPaint, width, height);
        }
        mTextPaint.setColor(powder.mColor);

        // ascent() is negative
        float baseline = -mTextPaint.ascent() + 2.5f;
        mCanvas.drawText(powder.mPowder, 2.5f, baseline, mTextPaint);
        return bitmap;
    }

    private void drawBorder(Canvas canvas, TextPaint paint, int width, int height) {
        Paint.Style style = paint.getStyle();
        float strokeWidth = paint.getStrokeWidth();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(DensityUtil.dip2px(BarrageContext.gContext, 2f));
        paint.setColor(BarrageConfig.sBorderColor);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setStyle(style);
        paint.setStrokeWidth(strokeWidth);
    }


    public interface BuildMachine<CONTENT> {
        AbsDrawingCache<CONTENT> createDrawingCache(Bitmap bitmap);
    }
}
