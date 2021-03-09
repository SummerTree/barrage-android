package tv.athena.live.barrage.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.config.GLBarrageAdapter;
import tv.athena.live.barrage.glutils.CatchError;
import tv.athena.live.barrage.glutils.tools.Camera;
import tv.athena.live.barrage.newcache.AbsDrawingCache;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.render.shader.BarrageShader;
import tv.athena.live.barrage.stencil.StencilManager;
import tv.athena.live.barrage.trace.AbsTrace;
import tv.athena.live.barrage.utils.GLCoordinate;
import tv.athena.live.barrage.view.IGLBarrageView;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * OpenGL渲染Render
 *
 * @author donghao
 */
public class GLBarrageRender extends BaseGLBarrageRender implements GLSurfaceView.Renderer {

    private static final String TAG = BarrageConfig.TAG_RENDER;

    private float mBlue = 1.0f;

    private SmoothDeltaTime mSmoothDelta;
    private Camera mCamera;
    private BarrageShader mShader;

    private IGLBarrageView mBarrageView;

    private AtomicInteger mOrientation;

    private GLBulletTrace mStencilGLAnimation;

    private GLBulletTrace mRectStencilGLAnimation;

    /**
     * 因为每一次mSurfaceChanged都会触发一次onDraw，导致小窗大小不断变化时的弹幕异常，所以加上标识控制一下
     **/
    private boolean mSurfaceChanged = false;

    public GLBarrageRender(IGLBarrageView iBarrageView, int type, boolean autoIncrease, int orientation, float alpha) {
        super(iBarrageView, type, autoIncrease, orientation, alpha);

        mBarrageView = iBarrageView;
//        JSONObject data = ArkValue.gArkExtConfig.data();
//        mBlue = (data != null && data.has("GLBarrage_Blue")) ? mBlue : 0.0f;
        mBlue = 0.0f;
        mOrientation = new AtomicInteger(orientation);
        BarrageLog.info(TAG, "init mBarrageOn false, mOrientation 0");
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        BarrageLog.info(TAG, "render created");

        if (mShader != null) {
            GLES20.glClearColor(0.0f, 0.0f, mBlue, mBlue);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }

        delete();

        initRender();

        resetStencil();
        resetRectStencil();
        StencilManager.getInstance().reset();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        BarrageLog.info(TAG, "render changed width %d height %d orientation %d barrage type %d",
                width, height, mOrientation.get(), getBarrageType()
        );

        setRect(0, 0, width, height);
        GLCoordinate.setWorldSize(width, height);

        GLES20.glViewport(0, 0, width, height);
        float ratio = 1.0f * width / height;
        mCamera.sharpFocusing(-ratio, ratio);

        ceaseFire(true, false);

        this.setBarrageType(getBarrageType());

        mSurfaceChanged = true;

        resetSmooth();
        resetStencil();
        resetRectStencil();
        StencilManager.getInstance().reset();
    }

    private int mDrawIndex = 1;
    private long mLastTime = 0;
    private int mLastIndex = 0;

    private void printRefreshFPS() {
        if (BarrageConfig.isBarrageRefreshPrint()) {
            mDrawIndex++;
            if (mLastTime != 0 && (System.currentTimeMillis() - mLastTime) > 1000) {
                mLastTime = System.currentTimeMillis();
                mBarrageView.showToast("" + (mDrawIndex - mLastIndex));
                BarrageLog.info(TAG, "framefps: %d", ((mDrawIndex - mLastIndex)));
                mLastIndex = mDrawIndex;
            } else if (mLastTime == 0) {
                mLastTime = System.currentTimeMillis();
                mLastIndex = mDrawIndex;
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        printRefreshFPS();

        //这里必须每一次调用，不然许多手机存在花屏现象
        GLES20.glClearColor(0.0f, 0.0f, mBlue, mBlue);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);


        if (mBarrageView.isNeedClearEnable() && GLBarrageAdapter.needClear()) {
            BarrageLog.info(TAG, "needClear");
            GLBarrageAdapter.setHasClean();
            return;
        }

        if (!mSurfaceChanged && isBarrageRenderOn()) {
            //当屏幕不在有弹幕滚动时候，可以关闭弹幕，节省电量
            boolean isEmpty = drawFrames(mShader, mCamera, mSmoothDelta.getSmoothDelta());
            if (mBarrageView.isStencilEnable() && BarrageConfig.getAntiBlockNowStatus() && StencilManager.getInstance().hasData()) {
                stencilDraw();
            } else {
                resetStencil();
            }

            if (StencilManager.getInstance().hasRectStencil()) {
                stencilRectDraw();
//                BarrageLog.info(TAG, "stencilRectDraw");
            } else {
                resetRectStencil();
//                BarrageLog.info(TAG, "resetRectStencil");
            }

            if (isEmpty) {
                BarrageLog.info(TAG, "onDrawFrame switchRender false");
                mBarrageView.switchRender(false);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    drawEmpty(mShader, mCamera);
                }
            }

            mSmoothDelta.calcSmoothDelta();
            mSmoothDelta.recordRenderingTime();
            CatchError.catchError("barrage render draw frame");
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                drawEmpty(mShader, mCamera);
                GLES20.glClearColor(0.0f, 0.0f, mBlue, mBlue);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            }
        }

        mSurfaceChanged = false;
    }

    @Override
    public void offer(GunPowder gunPowder, int type) {
        if (gunPowder.mRawBitmap != null) {
            // 内容来源于图，预处理为需要的cache，并解除引用
            gunPowder.mCacheObject = new AbsDrawingCache.GLDrawingCache(gunPowder.mRawBitmap);
            gunPowder.mRawBitmap = null;
        }
        super.offer(gunPowder, type);
    }

    private void resetRectStencil() {
        if (mRectStencilGLAnimation != null) {
            mRectStencilGLAnimation.recycle();
            mRectStencilGLAnimation = null;
        }
    }

    /**
     * 镂空rect区域
     */
    private void stencilRectDraw() {
        if (mRectStencilGLAnimation == null) {
            ByteBuffer byteBuffer = StencilManager.getInstance().getRectByteBuffer();
            if (byteBuffer != null) {
                mRectStencilGLAnimation = createRectStencilGLAnimation(byteBuffer);
            }
        }

        if (mRectStencilGLAnimation != null) {
            stencilDraw(mShader, mCamera, mRectStencilGLAnimation);
        }
    }

    private GLBulletTrace createRectStencilGLAnimation(ByteBuffer byteBuffer) {
//        BarrageLog.info(TAG, "createRectStencilGLAnimation1");
        int width = StencilManager.getInstance().getScreenWidth();
        int height = StencilManager.getInstance().getScreenHeight();

        GLBulletTrace glAnimation = new GLBulletTrace(byteBuffer, StencilManager.DRAW_AREA_WIDTH, StencilManager.DRAW_AREA_HEIGHT);
        glAnimation.setGLXY(0, 0);
        glAnimation.setGLScaleXY((float) width / (float) StencilManager.DRAW_AREA_WIDTH,
                (float) height / (float) StencilManager.DRAW_AREA_HEIGHT);
        return glAnimation;
    }

    private void resetStencil() {
        if (mStencilGLAnimation != null) {
            mStencilGLAnimation.recycle();
            mStencilGLAnimation = null;
        }
    }

    /**
     * 画弹幕遮罩区域
     */
    private void stencilDraw() {
        ByteBuffer byteBuffer = StencilManager.getInstance().getStencilData();
        if (byteBuffer != null) {
            if (mStencilGLAnimation != null) {
                mStencilGLAnimation.recycle();
            }
            mStencilGLAnimation = createStencilGLAnimation(byteBuffer);
            StencilManager.getInstance().recycleByteBuffer(byteBuffer);
        }
        StencilManager.getInstance().activateStencilDraw();

        if (mStencilGLAnimation != null) {
            stencilDraw(mShader, mCamera, mStencilGLAnimation);
        }
    }

    /**
     * 创建弹幕遮罩GL环境
     *
     * @param byteBuffer 遮罩区域数据
     * @return GLTrace
     */
    private GLBulletTrace createStencilGLAnimation(ByteBuffer byteBuffer) {
        int width = StencilManager.getInstance().getVideoWidth();
        int height = StencilManager.getInstance().getVideoHeight();
        int originalX = StencilManager.getInstance().getOriginalPointX();
        int originalY = StencilManager.getInstance().getOriginalPointY();

        GLBulletTrace glAnimation = new GLBulletTrace(byteBuffer, StencilManager.DRAW_AREA_WIDTH, StencilManager.DRAW_AREA_HEIGHT);
        glAnimation.setGLXY(originalX, originalY);
        glAnimation.setGLScaleXY((float) width / (float) StencilManager.DRAW_AREA_WIDTH, (float) height / (float) StencilManager.DRAW_AREA_HEIGHT);

        return glAnimation;
    }

    public void resume() {
        if (mSmoothDelta != null) {
            mSmoothDelta.reset();
        }
    }

    //android doc said when the EGL context is lost,
    //all OpenGL resources associated with that context will be automatically deleted
    @SuppressWarnings("unused")
    @Override
    public void delete() {
        super.delete();

        if (null != mShader) {
            mShader.destroy();
            mShader = null;
        }

    }

    @Override
    public void ceaseFire(boolean clearAll) {
        resetStencil();
        resetRectStencil();
        super.ceaseFire(clearAll);
    }

    @Override
    public void onRequireMarqueeInSurface(Bitmap text, float startX, long duration) {
        createGLAnimation(text).x(startX, -text.getWidth()).y(0, 0).duration(duration).start(this);
    }

    @Override
    public void notifyDispSizeChanged(int i, int i1) {

    }

    @Override
    public boolean isStop() {
        return false;
    }

    @Override
    public void queueEvent(Runnable task) {

    }

    @Override
    public long getCurrentTime() {
        return 0;
    }

    @Override
    public void setBarrageAlpha(float alpha) {
        setAlpha(alpha);
    }


    @Override
    public void setOrientation(int orientation, boolean isFromFloating) {
        super.setOrientation(orientation, isFromFloating);
        mOrientation.set(orientation);
    }

    private void initRender() {
        mShader = new BarrageShader();
        mShader.use();

        mCamera = new Camera(GLCoordinate.TOP, GLCoordinate.BOTTOM, GLCoordinate.NEAR, GLCoordinate.FAR, GLCoordinate.EYE_Z);
        mCamera.setUp();

        GLES20.glEnableVertexAttribArray(mShader.getPosHandle());
        GLES20.glEnableVertexAttribArray(mShader.getTexHandle());

        initHolderGL();

        mSmoothDelta = new SmoothDeltaTime();

        CatchError.catchError("barrage render init");
    }

    @Override
    public void setBarrageRenderOn(boolean isRenderOn) {
        super.setBarrageRenderOn(isRenderOn);
        GLBarrageAdapter.setRenderOn(isRenderOn);
    }

    @Override
    protected AbsTrace createTrace(BulletBuilder.Bullet<ByteBuffer> bullet, int target) {
        return new GLBulletTrace(bullet, target);
    }

    @Override
    protected float toCustomWorldPositionX(float positionX) {
        return GLCoordinate.toWorldPositionX(positionX);
    }

    @Override
    protected float toCustomWorldPositionY(float positionY) {
        return GLCoordinate.toWorldPositionY(positionY);
    }

    @Override
    public void resetSmooth() {
        if (mSmoothDelta != null) {
            mSmoothDelta.reset();
        }
    }


}
