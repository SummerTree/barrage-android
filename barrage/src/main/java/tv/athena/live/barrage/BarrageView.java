package tv.athena.live.barrage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.render.BarrageRender;
import tv.athena.live.barrage.render.IBarrageRender;
import tv.athena.live.barrage.render.draw.DrawHelper;
import tv.athena.live.barrage.report.BarrageCacheForReport;
import tv.athena.live.barrage.utils.BarrageFpsHelper;
import tv.athena.live.barrage.view.IBarrageViewController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author donghao on 2018/9/6.
 */

public abstract class BarrageView extends View implements IBarrageViewController {

    private static final String TAG = BarrageView.class.getSimpleName();

    protected AtomicInteger mModel;

    private IBarrageRender mRender;

    protected volatile BarrageRender mHandler;

    private final Object mDrawMonitor = new Object();

    private boolean mDrawFinished = false;

    private boolean isSurfaceCreated;

    private BarrageFpsHelper mBarrageFpsHelper = BarrageFpsHelper.create();


    public BarrageView(Context context) {
        super(context);
//        NativeBitmapFactory.loadLibs();
        initBarrageView();
    }

    public BarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBarrageView();
    }

    public BarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBarrageView();
    }

    private void initBarrageView() {
        initBarrageModel();

        mRender = new BarrageRender(this, BarrageConfig.modelToType(getBarrageModel()),
                BarrageConfig.ModelLuxury == getBarrageModel(),
                getResources().getConfiguration().orientation,
                getInitAlpha(), getInitSize());

        BarrageCacheForReport.getInstance().switchReport(false);

    }

    protected void initBarrageModel() {
        mModel = new AtomicInteger(BarrageConfig.getBarrageModel());
    }

    protected int getBarrageModel() {
        return mModel.get();
    }

    protected float getInitAlpha() {
        return BarrageConfig.getBarrageAlpha();
    }

    protected int getInitSize() {
        return BarrageConfig.DEFAULT_BARRAGE_SIZE;
    }

    @Override
    public boolean isQueueFixed() {
        return false;
    }

    @Override
    public int getQueueLine() {
        return 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRender.isBarrageOn() && mRender.isBarrageRenderOn()) {
            mRender.draw(canvas);
            mBarrageFpsHelper.update();
            if (BarrageConfig.isBarrageRefreshPrint()) {
                DrawHelper.drawFPS(canvas, mBarrageFpsHelper.getFpsStr());
            }
            unlockCanvasAndPost();
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mRender != null) {
            mRender.notifyDispSizeChanged(right - left, bottom - top);
        }
        isSurfaceCreated = true;
    }

    @Override
    public IBarrageRender getRender() {
        return mRender;
    }

    @Override
    public void switchRender(boolean isOpen) {
        BarrageLog.info(TAG, "switchRender %b", isOpen);
        if (isOpen) {
            mRender.resetSmooth();
            mRender.start();
        } else {
            mRender.stop();
            unlockCanvasAndPost();
        }
        mRender.setBarrageRenderOn(isOpen);
    }

    @Override
    public void offerGunPowder(GunPowder gunPowder, int type) {
        mRender.offer(gunPowder, type);
    }

    @Override
    public void ceaseFire(boolean cleanAll) {
        getRender().ceaseFire(cleanAll);
    }

    @Override
    public void onBarrageAlphaChanged(BarrageEvent.BarrageAlphaChanged arg0) {
        mRender.setBarrageAlpha(arg0.arg0);
    }

    @Override
    public void onBarrageSizeChanged(BarrageEvent.BarrageSizeChanged arg0) {
        mRender.onBarrageSizeChanged(arg0.arg0);
    }

    @Override
    public void onBarrageModelChanged(BarrageEvent.BarrageModelChanged arg0) {
        updateBarrageModel(arg0.mode);
    }

    protected void updateBarrageModel(final int model) {
        if (model != getBarrageModel()) {
            final int last = getBarrageModel();

            mModel.set(model);
            int type = BarrageConfig.modelToType(model);
            mRender.setAutoIncrease(type, BarrageConfig.ModelLuxury == model);
            mRender.setBarrageType(type);

            if (BarrageConfig.ModelClose == model) {
                mRender.ceaseFire(true);
            } else if (BarrageConfig.ModelClose == last) {
                mRender.resetSmooth();
                switchRender(true);
            }
        }
    }

    @Override
    public boolean hasCustomMargin() {
        return false;
    }

    private void unlockCanvasAndPost() {
        synchronized (mDrawMonitor) {
            mDrawFinished = true;
            mDrawMonitor.notifyAll();
        }
    }

    protected void lockCanvas() {
        postInvalidateCompat();
        synchronized (mDrawMonitor) {
            while ((!mDrawFinished) && (mRender != null)) {
                try {
                    mDrawMonitor.wait(200);
                } catch (InterruptedException e) {
                    if (mRender == null || mRender.isStop()) {
                        break;
                    } else {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            mDrawFinished = false;
        }
    }

    @SuppressLint("NewApi")
    private void postInvalidateCompat() {
        if (Build.VERSION.SDK_INT >= 16) {
            this.postInvalidateOnAnimation();
        } else {
            this.postInvalidate();
        }
    }

    @Override
    public long drawDanmakus() {
        if (!isSurfaceCreated) {
            return 0;
        }
        if (!isShown()) {
            return -1;
        }
        long stime = SystemClock.elapsedRealtime();
        lockCanvas();
        return SystemClock.elapsedRealtime() - stime;
    }

    @Override
    public boolean isViewReady() {
        return isSurfaceCreated;
    }

    @Override
    public void clearCanvas() {
        //FIXME DO NOTHING???
    }

    @Override
    public float getFps() {
        return mBarrageFpsHelper.fps();
    }
}
