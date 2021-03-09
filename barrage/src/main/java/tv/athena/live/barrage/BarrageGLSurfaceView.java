package tv.athena.live.barrage;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.widget.Toast;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.render.GLBarrageRender;
import tv.athena.live.barrage.render.IBarrageRender;
import tv.athena.live.barrage.report.BarrageCacheForReport;
import tv.athena.live.barrage.view.IGLBarrageView;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 与业务无关实现的OpenGL弹幕渲染方式
 *
 * @author donghao on 2018/9/5.
 */

public abstract class BarrageGLSurfaceView extends GLSurfaceView implements IGLBarrageView {
    public static final String TAG = BarrageConfig.TAG_VIEW;

    protected AtomicInteger mModel;
    protected IBarrageRender mRender;

    /**
     * false每次新打开直播间时，竖屏弹幕延时接收并显示弹幕, true表示不需要延迟显示，直接显示
     **/
    private boolean mHasDelay = true;
    /**
     * 延迟弹幕显示delay时间
     **/
    private static final int DELAY_FIRE_TIME = 2500;
    private Runnable mDelayFireBarrage;

    private int mCount = 0;

    public BarrageGLSurfaceView(Context context) {
        super(context);
    }

    public BarrageGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void initGLBarrageView(Context context) {
        initSurfaceConfig();
        initBarrageModel();

        GLBarrageRender glBarrageRender = new GLBarrageRender(this, BarrageConfig.modelToType(getBarrageModel()),
                BarrageConfig.ModelLuxury == getBarrageModel(),
                getResources().getConfiguration().orientation,
                getInitAlpha());
        mRender = glBarrageRender;

        setRenderer(glBarrageRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        BarrageCacheForReport.getInstance().switchReport(true);
    }

    protected void initSurfaceConfig() {
        setEGLContextClientVersion(2);
        setZOrderOnTop(false);
        setZOrderMediaOverlay(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    protected void initBarrageModel() {
        mModel = new AtomicInteger(BarrageConfig.getBarrageModel());
    }

    public void delayFireBarrage(boolean needShownImmediately, boolean isFromFloating) {
        if (!needShownImmediately && !isFromFloating && !mHasDelay) {
            if (null == mDelayFireBarrage) {
                mDelayFireBarrage = new Runnable() {
                    @Override
                    public void run() {
                        cancelDelayFireBarrage();
                    }
                };
            }
            mHasDelay = false;
            postDelayed(mDelayFireBarrage, DELAY_FIRE_TIME);
            BarrageLog.debug(TAG, "delayFireBarrage");
        } else {
            cancelDelayFireBarrage();
        }
    }

    public void cancelDelayFireBarrage() {
        mHasDelay = true;
        if (null != mDelayFireBarrage) {
            removeCallbacks(mDelayFireBarrage);
            mDelayFireBarrage = null;
        }
        fire();
        BarrageLog.debug(TAG, "cancelDelayFireBarrage");
    }


    @Override
    public synchronized void switchRender(boolean isOpen) {
        BarrageLog.info(TAG, "switchRender %b", isOpen);
        if (isOpen) {
            mRender.resetSmooth();
            setRenderMode(RENDERMODE_CONTINUOUSLY);
        } else {
            setRenderMode(RENDERMODE_WHEN_DIRTY);
            requestRender();
        }
        mRender.setBarrageRenderOn(isOpen);
    }

    /**
     * 清空并且移除弹幕资源
     *
     * @param clearAll 是否需要清空弹幕和跑马灯，否则只清空弹幕
     */
    @Override
    public void ceaseFire(final boolean clearAll) {
        BarrageLog.info(TAG, "enter ceaseFire: %b", clearAll);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (null != mRender) {
                    mRender.ceaseFire(clearAll);
                }
            }
        });
    }

    @Override
    public void showToast(final String content) {
        post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BarrageContext.gContext, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean hasCustomMargin() {
        return false;
    }

    @Override
    public void offerGunPowder(@NotNull GunPowder gunPowder, int type) {
        mRender.offer(gunPowder, type);
    }

    @Override
    public boolean isStencilEnable() {
        return true;
    }

    @Override
    public boolean isNeedClearEnable() {
        return true;
    }

    /**
     * 透明度设置
     *
     * @param arg0
     */
    @Override
    public void onBarrageAlphaChanged(BarrageEvent.BarrageAlphaChanged arg0) {
        BarrageLog.info(TAG, "onBarrageAlphaChanged , alpha = " + arg0.arg0);
        setBarrageAlpha(arg0.arg0);
    }

    protected void setBarrageAlpha(final float alpha) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRender.setBarrageAlpha(alpha);
            }
        });
    }

    /**
     * 字体大小设置
     *
     * @param arg0
     */
    @Override
    public void onBarrageSizeChanged(BarrageEvent.BarrageSizeChanged arg0) {
        final int size = arg0.arg0;
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRender.onBarrageSizeChanged(size);
            }
        });
    }

    /**
     * 弹幕模式设置
     *
     * @param arg0
     */
    @Override
    public void onBarrageModelChanged(BarrageEvent.BarrageModelChanged arg0) {
        final int model = arg0.mode;
        updateBarrageModel(model);
    }

    protected void updateBarrageModel(final int model) {
        BarrageLog.debug(TAG, "updateBarrageModel, model = %d", model);

        if (model != getBarrageModel()) {
            final int last = getBarrageModel();

            mModel.set(model);
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    int type = BarrageConfig.modelToType(model);
                    mRender.setAutoIncrease(type, BarrageConfig.ModelLuxury == model);
                    mRender.setBarrageType(type);

                    if (BarrageConfig.ModelClose == model) {
                        // 弹幕模式变化后，只需要清空弹幕
                        ceaseFire(false);
                    } else if (BarrageConfig.ModelClose == last) {
                        mRender.resetSmooth();
                        fireIfNeed();
                    }
                }
            });
        }
    }

    protected void fireIfNeed() {
        ++mCount;
        if (mCount > 100) {
            mCount = 0;
            BarrageLog.info(TAG, "barrage Model = %d , isBarrageOn = %b , mHasDelay = %b", getBarrageModel(), mRender.isBarrageOn(), mHasDelay);
        }
        fire();
    }

    private void fire() {
        if (BarrageConfig.ModelClose != getBarrageModel()
                && mRender.isBarrageOn()
                && isCanDelay()) {
            if (!isRenderOpen()) {
                switchRender(true);
            }
        }
    }

    protected boolean isCanDelay() {
        return mHasDelay;
    }

    protected boolean isRenderOpen() {
        return getRenderMode() == RENDERMODE_CONTINUOUSLY;
    }

    protected int getBarrageModel() {
        return mModel.get();
    }

    protected IBarrageRender getRender() {
        return mRender;
    }

    protected AtomicInteger getModel() {
        return mModel;
    }

    protected float getInitAlpha() {
        return BarrageConfig.getBarrageAlpha();
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
