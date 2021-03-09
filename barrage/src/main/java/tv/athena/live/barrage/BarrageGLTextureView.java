package tv.athena.live.barrage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.TextureView;

import tv.athena.live.barrage.annotation.RenderThread;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.render.BarrageRender;
import tv.athena.live.barrage.render.IBarrageRender;
import tv.athena.live.barrage.render.draw.DrawHelper;
import tv.athena.live.barrage.utils.BarrageFpsHelper;
import tv.athena.live.barrage.view.IBarrageViewController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mylhyz on 2018/12/17.
 */

public abstract class BarrageGLTextureView extends TextureView implements IBarrageViewController, TextureView.SurfaceTextureListener {

    private static final String TAG = "BarrageGLTextureView";

    /**
     * 弹幕模式（华丽，半屏，无）
     */
    protected AtomicInteger mModel;

    /**
     * 弹幕渲染器
     */
    private IBarrageRender mRender;

    /**
     * 是否Surface已经创建完成
     */
    private boolean mIsSurfaceCreated;
    /**
     * FPS帧率统计
     */
    private BarrageFpsHelper mBarrageFpsHelper = BarrageFpsHelper.create();

    public BarrageGLTextureView(Context context) {
        super(context);
        initBarrageView();
    }

    public BarrageGLTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBarrageView();
    }

    public BarrageGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBarrageView();
    }

    private void initTextureView() {
        setLayerType(LAYER_TYPE_HARDWARE, null);

        //设置TextureView内容透明
        setOpaque(false);

        setWillNotCacheDrawing(true);
        setDrawingCacheEnabled(false);

        initTextureListener();
    }

    /**
     * 设置TextureView的Surface事件监听
     */
    private void initTextureListener() {
        setSurfaceTextureListener(this);
    }


    private void initBarrageView() {

//        NativeBitmapFactory.loadLibs();

        initTextureView();
        initBarrageModel();

        mRender = new BarrageRender(this, BarrageConfig.modelToType(getBarrageModel()),
                BarrageConfig.ModelLuxury == getBarrageModel(),
                getResources().getConfiguration().orientation,
                getInitAlpha(), getInitSize());
    }

    /**
     * 初始化弹幕模式，从配置读取（关弹幕，华丽，半屏）
     */
    protected void initBarrageModel() {
        mModel = new AtomicInteger(BarrageConfig.getBarrageModel());
    }

    protected int getBarrageModel() {
        return mModel.get();
    }

    protected float getInitAlpha() {
        return BarrageConfig.getBarrageAlpha();
    }

    /**
     * 获取弹幕尺寸，默认从配置读取（如果覆盖该方法，则需要自己实现大小计算）
     *
     * @return 读取配置得到的尺寸
     */
    protected int getInitSize() {
        return BarrageConfig.DEFAULT_BARRAGE_SIZE;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mRender != null) {
            mRender.notifyDispSizeChanged(width, height);
        }
        mIsSurfaceCreated = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        if (mRender != null) {
            mRender.notifyDispSizeChanged(width, height);
        }
        mIsSurfaceCreated = true;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        mIsSurfaceCreated = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public IBarrageRender getRender() {
        return mRender;
    }

    @Override
    public void switchRender(boolean isOpen) {
        BarrageLog.debug(TAG, "switchRender=%b", isOpen);
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
    @RenderThread
    public long drawDanmakus() {
        if (!mIsSurfaceCreated) {
            return 0;
        }
        if (!isShown()) {
            return -1;
        }
        long stime = SystemClock.elapsedRealtime();
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            if (mRender != null && mRender.isBarrageOn() && mRender.isBarrageRenderOn()) {
                mRender.draw(canvas);
                mBarrageFpsHelper.update();
                if (BarrageConfig.isBarrageRefreshPrint()) {
                    DrawHelper.drawFPS(canvas, mBarrageFpsHelper.getFpsStr());
                }
            }
            if (mIsSurfaceCreated) {
                //如果页面已经销毁了，则不能再进行渲染
                unlockCanvasAndPost(canvas);
            }
        }
        return SystemClock.elapsedRealtime() - stime;
    }

    @Override
    public boolean isViewReady() {
        return mIsSurfaceCreated;
    }

    @Override
    @RenderThread
    public void clearCanvas() {
        if (!isViewReady()) {
            return;
        }
        Canvas canvas = lockCanvas();
        if (canvas != null) {
            DrawHelper.clearCanvas(canvas);
            unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public float getFps() {
        return mBarrageFpsHelper.fps();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        NativeBitmapFactory.releaseLibs();
    }

    @Override
    public boolean isQueueFixed() {
        return false;
    }

    @Override
    public int getQueueLine() {
        return 0;
    }
}
