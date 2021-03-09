package tv.athena.live.barrage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
 * @author yaojun on 2019/4/1.
 */

public abstract class BarrageSurfaceView extends SurfaceView implements IBarrageViewController, SurfaceHolder.Callback {

    private static final String TAG = BarrageSurfaceView.class.getSimpleName();

    protected AtomicInteger mModel;

    private IBarrageRender mRender;

    private SurfaceHolder mSurfaceHolder;

    private boolean mSurfaceCreated;

    private BarrageFpsHelper mBarrageFpsHelper = BarrageFpsHelper.create();


    public BarrageSurfaceView(Context context) {
        super(context);
        init();
        initBarrageView();
    }

    private void init() {
        setZOrderMediaOverlay(true);
        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);
        setWillNotDraw(true);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        DrawHelper.useDrawColorToClearCanvas(true, true);
    }

    private void initBarrageView() {
        initBarrageModel();

        mRender = new BarrageRender(this, BarrageConfig.modelToType(getBarrageModel()),
                BarrageConfig.ModelLuxury == getBarrageModel(),
                getResources().getConfiguration().orientation,
                getInitAlpha(), getInitSize()) {
            @Override
            protected void realDrawFrame(Canvas canvas, BulletTrace bulletTrace, Paint paint) {
                canvas.drawBitmap(bulletTrace.mBullet.getCacheObject().getContent(), bulletTrace.getCurrentFrame().mX, bulletTrace.getCurrentFrame().mY, paint);
            }
        };

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

    @Override
    public long drawDanmakus() {
        if (!mSurfaceCreated) {
            return 0;
        }
        if (!isShown()) {
            return -1;
        }
        long stime = SystemClock.uptimeMillis();
        if (mRender != null && mRender.isBarrageOn() && mRender.isBarrageRenderOn()) {
            try {
                final Canvas canvas = mSurfaceHolder.lockCanvas();
                if (canvas != null) {
                    mRender.draw(canvas);
                    mBarrageFpsHelper.update();
                    if (BarrageConfig.isBarrageRefreshPrint()) {
                        DrawHelper.drawFPS(canvas, mBarrageFpsHelper.getFpsStr());
                    }
                }
                if (mSurfaceCreated) {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            } catch (Exception e) {
                BarrageLog.error(TAG, "BarrageSurfaceView.drawDanmakus error, %s", e);
            }
        }

        return SystemClock.uptimeMillis() - stime;
    }


    @Override
    public boolean isViewReady() {
        return mSurfaceCreated;
    }

    @Override
    public void clearCanvas() {
        //FIXME DO NOTHING???
    }

    @Override
    public float getFps() {
        return mBarrageFpsHelper.fps();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = holder.lockCanvas();
        if (canvas != null) {
            DrawHelper.clearCanvas(canvas);
            holder.unlockCanvasAndPost(canvas);
        }
        if (mRender != null) {
            mRender.ceaseFire(true);
            mRender.resetSmooth();
        }
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mRender != null) {
            mRender.notifyDispSizeChanged(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
        if (mRender != null) {
            mRender.ceaseFire(true);
            mRender.resetSmooth();
        }
    }

}
