package tv.athena.live.barrage.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.view.Choreographer;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.newcache.AbsDrawingCache;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.render.draw.DrawHelper;
import tv.athena.live.barrage.trace.AbsTrace;
import tv.athena.live.barrage.trace.TraceFrame;
import tv.athena.live.barrage.view.IBarrageViewController;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 普通BarrageRender实现
 * <p>
 * Android4.1之后增加了Choreographer机制，用于同Vsync机制配合，实现统一调度界面绘图
 * Note: 这里不考虑4.1之前版本
 *
 * @author donghao on 2018/9/6.
 */
public class BarrageRender extends AbsBarrageRender<BarrageRender.BulletTrace, Bitmap> {
    private static final String TAG = BarrageRender.class.getSimpleName();

    private static final HandlerThread RENDER_HANDLER_THREAD = BarrageConfig.newStartHandlerThread("BarrageRenderThread", Process.THREAD_PRIORITY_DEFAULT);

    private final static int START = 0;

    private final static int STOP = 1;

    private final static int UPDATE = 2;

    private final static int CLEAR_CANVAS = 3;

    private final static int RECT_CHANGED = 4;

    private final static int ALPHA = 5;

    private final Canvas mCanvas = new Canvas();

    private Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            mHandler.sendEmptyMessage(UPDATE);
        }
    };

    private IBarrageViewController mBarrageViewController;

    private Handler mHandler;

    private AtomicBoolean mStopped = new AtomicBoolean(false);

    /**
     * 弹幕总开关，模式转换而来
     */
    private AtomicBoolean mBarrageOn = new AtomicBoolean(true);

    private SmoothDeltaTime mSmoothDelta = new SmoothDeltaTime();

    private Paint mBitPaint;


    public BarrageRender(IBarrageViewController iBarrageView, int type, boolean autoIncrease, int orientation, float alpha, int size) {
        super(iBarrageView, type, autoIncrease, orientation, alpha, size);

        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);

        mBarrageOn.set(BarrageConfig.TYPE_NULL != getBarrageType());
        mBarrageViewController = iBarrageView;
        mHandler = new Handler(RENDER_HANDLER_THREAD.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case START:
                        mSmoothDelta.start();
                    case UPDATE:
                        updateInChoreographer();
                        break;
                    case STOP:
                        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
                    case CLEAR_CANVAS:
                        mBarrageViewController.clearCanvas();
                        break;
                    case RECT_CHANGED:
                        int width = msg.arg1;
                        int height = msg.arg2;
                        setRect(0, 0, width, height);
                        break;
                    case ALPHA:
                        setAlpha((Float) msg.obj);
                        break;
                    default:
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void start() {
        mHandler.removeCallbacksAndMessages(null);
        mStopped.set(false);
        mHandler.sendEmptyMessage(UPDATE);
    }

    @Override
    public void stop() {
        mStopped.set(true);
        mHandler.sendEmptyMessage(STOP);
    }

    @Override
    public boolean isStop() {
        return mStopped.get();
    }

    @Override
    public void offer(GunPowder gunPowder, int type) {
        if (!mBarrageViewController.isViewReady()) {
            //View尚未准备好时不添加
            return;
        }
        if (gunPowder.mRawBitmap != null) {
            final Bitmap newBitmap = mBulletBuilder.getBitmapManager().get(gunPowder.mRawBitmap.getWidth(), gunPowder.mRawBitmap.getHeight());
            mCanvas.setBitmap(newBitmap);
            mCanvas.drawBitmap(gunPowder.mRawBitmap, new Matrix(), null);
            gunPowder.mRawBitmap = null;
            gunPowder.mCacheObject = new AbsDrawingCache.ViewDrawingCache(newBitmap);
        }
        super.offer(gunPowder, type);
    }

    @Override
    public void ceaseFire(final boolean clearAll) {
        //FIXME 注意由于底层设计到对非线程安全的动画列表操作，需要post到绘制线程进行操作 by mylhyz
        queueEvent(new Runnable() {
            @Override
            public void run() {
                BarrageRender.super.ceaseFire(clearAll);
            }
        });
    }

    /**
     * post到绘制线程
     *
     * @param task 任务
     */
    public void queueEvent(Runnable task) {
        mHandler.post(task);
    }

    @Override
    public long getCurrentTime() {
        return mSmoothDelta.getCurrentTime();
    }

    @Override
    public void resetSmooth() {
        if (mSmoothDelta != null) {
            mSmoothDelta.reset();
        }
    }

    @Override
    public void setBarrageAlpha(float alpha) {
        mHandler.obtainMessage(ALPHA, alpha).sendToTarget();
    }

    @Override
    public void setBarrageType(int type) {
        super.setBarrageType(type);
        if (BarrageConfig.TYPE_NULL != getBarrageType()) {
            mHandler.sendEmptyMessage(START);
        } else {
            mHandler.sendEmptyMessage(STOP);
        }
    }

    @Override
    public boolean isBarrageOn() {
        return mBarrageOn.get();
    }

    @Override
    public void onRequireMarqueeInSurface(Bitmap bitmap, float widthPixels, long duration) {

    }

    @Override
    public void notifyDispSizeChanged(int width, int height) {
        mHandler.obtainMessage(RECT_CHANGED, width, height).sendToTarget();
    }

    @Override
    protected BulletTrace createTrace(BulletBuilder.Bullet<Bitmap> bullet, int target) {
        return new BulletTrace(bullet, target);
    }

    @Override
    public void clearCanvas() {
        mHandler.obtainMessage(CLEAR_CANVAS).sendToTarget();
    }

    @Override
    public void draw(Canvas canvas) {
//        Trace.beginSection("BarrageRender.draw");
        if (mBarrageViewController.isViewReady()) {
            DrawHelper.clearCanvas(canvas);
        }

        recycleUnusedFrame();
        calculateBarrage(mSmoothDelta.getSmoothDelta());
        drawCurrentFrame(canvas);
        mSmoothDelta.recordRenderingTime();

//        Trace.endSection();
    }

    protected void drawCurrentFrame(Canvas canvas) {

//        Trace.beginSection("BarrageRender_drawCurrentFrame");

        List<BulletTrace> animations = getAnimations();

        for (int i = animations.size() - 1; i >= 0; i--) {
            BulletTrace an = animations.get(i);
            if (an.mIsOwn) {
                drawFrame(canvas, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            BulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive != GunPowder.EXPLOSIVE_HIGH && an.mExplosive != GunPowder.EXPLOSIVE_HIGH_SHOW) {
                drawFrame(canvas, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            BulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive == GunPowder.EXPLOSIVE_HIGH) {
                drawFrame(canvas, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            BulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive == GunPowder.EXPLOSIVE_HIGH_SHOW) {
                drawFrame(canvas, an);
            }
        }
    }

    private void drawFrame(Canvas canvas, BulletTrace bulletTrace) {

        final Bitmap bitmap = bulletTrace.mBullet.getCacheObject().getContent();
        if (bitmap == null || bitmap.isRecycled()) {
            BarrageLog.error(TAG, "bitmap isnull or isRecycled");
            return;
        }

        final Paint paint = new Paint(mBitPaint);
        paint.setAlpha((int) (getAlpha() * 255));

        if (mBarrageViewController.isViewReady()) {
            realDrawFrame(canvas, bulletTrace, paint);
        }
    }

    protected void realDrawFrame(Canvas canvas, BulletTrace bulletTrace, Paint paint) {
        final Bitmap bitmap = bulletTrace.mBullet.getCacheObject().getContent();
        final Rect dest = new Rect((int) bulletTrace.getCurrentFrame().mX, (int) bulletTrace.getCurrentFrame().mY,
                (int) (bitmap.getWidth() * getScale() + bulletTrace.getCurrentFrame().mX), (int) (bitmap.getHeight() * getScale() + bulletTrace.getCurrentFrame().mY));

        canvas.drawBitmap(bitmap, null, dest, paint);
    }


    private void updateInChoreographer() {
        if (isStop() || !mBarrageViewController.isViewReady()) {
            BarrageLog.info(TAG, "updateInChoreographer return");
            return;
        }
        Choreographer.getInstance().postFrameCallback(mFrameCallback);
        long d = mSmoothDelta.calcSmoothDelta();
        if (d < 0) {
            mHandler.removeMessages(UPDATE);
            return;
        }
        d = mBarrageViewController.drawDanmakus();
        mSmoothDelta.onDrawCost(d);
        mHandler.removeMessages(UPDATE);
    }
//
//    @Override
//    protected void calculateCurrentFrameReal(BulletTrace an, float delta) {
//        int size = an.mHolds.length;
//        for (int i = 0; i < size; ++i) {
//            if (an.mBeginTime > 0) {
//                an.setCurrentFrame(i, an.mSpeeds[i] * (mSmoothDelta.getCurrentTime() - an.mBeginTime));
//            } else {
//                an.stepCurrentFrame(i, an.mSpeeds[i] * delta);
//            }
//        }
//        an.mCurrentTime += delta;
//    }

    @Override
    public AbsDrawingCache<Bitmap> createDrawingCache(Bitmap bitmap) {
        return new AbsDrawingCache.ViewDrawingCache(bitmap);
    }

    /**
     * 普通View弹幕动画元素
     */
    public static class BulletTrace extends AbsTrace {

        public final BulletBuilder.Bullet<Bitmap> mBullet;

        public BulletTrace(BulletBuilder.Bullet<Bitmap> bullet, int target) {
            exploreBullet(bullet);
            mTarget = target;
            mBullet = bullet;
        }

        @Override
        public void recycle() {
            if (mBullet != null) {
                mBullet.getCacheObject().decreaseReferenceCount();
            }
        }

        @Override
        public AbsTrace x(float start, float end) {
            mHolds[TraceFrame.X][0] = start;
            mHolds[TraceFrame.X][1] = end;
            return this;
        }

        @Override
        public AbsTrace y(float start, float end) {
            mHolds[TraceFrame.Y][0] = start;
            mHolds[TraceFrame.Y][1] = end;
            return this;
        }

    }
}
