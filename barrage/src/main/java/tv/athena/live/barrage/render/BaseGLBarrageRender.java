package tv.athena.live.barrage.render;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.glutils.CatchError;
import tv.athena.live.barrage.glutils.tools.Camera;
import tv.athena.live.barrage.glutils.tools.VBO;
import tv.athena.live.barrage.newcache.AbsDrawingCache;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.render.shader.BarrageShader;
import tv.athena.live.barrage.trace.AbsTrace;
import tv.athena.live.barrage.trace.TraceFrame;
import tv.athena.live.barrage.utils.GLCoordinate;
import tv.athena.live.barrage.view.IGLBarrageView;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;

/**
 * 基础GLRender
 *
 * @author donghao  on 2018/8/28.
 */

abstract class BaseGLBarrageRender extends AbsBarrageRender<BaseGLBarrageRender.GLBulletTrace, ByteBuffer> {

    private static final int Stride = (3 + 2) * 4;
    private static final int TexOffset = 3 * 4;


    private VBO mPosVBO = null;
    private VBO mDrawListVBO = null;
    private float[] mModelMatrix = new float[16];

    public BaseGLBarrageRender(IGLBarrageView iBarrageView, int type, boolean autoIncrease, int orientation, float alpha) {
        super(iBarrageView, type, autoIncrease, orientation, alpha, BarrageConfig.DEFAULT_BARRAGE_SIZE);
    }

    public GLBulletTrace createGLAnimation(Bitmap texture) {
        return new GLBulletTrace(texture);
    }

    public void initHolderGL() {
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
        //GLES20.glDepthMask(true);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        short drawOrder[] = {0, 1, 2, 2, 3, 0};
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer drawList = dlb.asShortBuffer();
        drawList.put(drawOrder);
        drawList.position(0);

        mDrawListVBO = new VBO(GLES20.GL_ELEMENT_ARRAY_BUFFER, drawOrder.length * 2, drawList, GLES20.GL_STATIC_DRAW);

        float[] vertex = {
                0, 0, GLCoordinate.NEAR_Z, 0.0f, 0.0f, // top left
                0, 1, GLCoordinate.NEAR_Z, 0.0f, 1.0f, // bottom left
                1, 1, GLCoordinate.NEAR_Z, 1.0f, 1.0f, // bottom right
                1, 0, GLCoordinate.NEAR_Z, 1.0f, 0.0f// top right
        };

        ByteBuffer vpb = ByteBuffer.allocateDirect(vertex.length * 4);
        vpb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexPos = vpb.asFloatBuffer();
        vertexPos.put(vertex);
        vertexPos.position(0);

        mPosVBO = new VBO(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, vertexPos, GLES20.GL_STATIC_DRAW);
    }

    public synchronized boolean drawFrames(BarrageShader shader, Camera camera, float delta) {
        recycleUnusedFrame();
        calculateBarrage(delta);
        drawCurrentFrame(shader, camera);
        return isEmpty();
    }


    protected void drawCurrentFrame(BarrageShader shader, Camera camera) {
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        List<GLBulletTrace> animations = getAnimations();

        for (int i = animations.size() - 1; i >= 0; i--) {
            GLBulletTrace an = animations.get(i);
            if (an.mIsOwn) {
                drawFrame(shader, camera, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            GLBulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive != GunPowder.EXPLOSIVE_HIGH && an.mExplosive != GunPowder.EXPLOSIVE_HIGH_SHOW) {
                drawFrame(shader, camera, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            GLBulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive == GunPowder.EXPLOSIVE_HIGH) {
                drawFrame(shader, camera, an);
            }
        }

        for (int i = animations.size() - 1; i >= 0; i--) {
            GLBulletTrace an = animations.get(i);
            if (!an.mIsOwn && an.mExplosive == GunPowder.EXPLOSIVE_HIGH_SHOW) {
                drawFrame(shader, camera, an);
            }
        }
    }

    protected void drawEmpty(BarrageShader shader, Camera camera) {
        camera.pressShutter(shader.getProjectionViewMatrixHandle(), shader.getModelMatrixHandle(), mModelMatrix);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPosVBO.getId());

        GLES20.glVertexAttribPointer(shader.getPosHandle(), 3, GLES20.GL_FLOAT, false, Stride, 0);
        GLES20.glVertexAttribPointer(shader.getTexHandle(), 2, GLES20.GL_FLOAT, false, Stride, TexOffset);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mDrawListVBO.getId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    private void drawFrame(BarrageShader shader, Camera camera, GLBulletTrace an) {
        Matrix.setIdentityM(mModelMatrix, 0);

        //  Matrix.translateM(mModelMatrix, 0, Coordinate.toGLXPosition(900f), Coordinate.toGLYPosition(0f), 0);

        Matrix.translateM(mModelMatrix, 0, an.mCurrentFrame.mX, an.mCurrentFrame.mY, 0);
        Matrix.scaleM(mModelMatrix, 0, an.mCurrentFrame.mScaleX * an.mGLWidth, an.mCurrentFrame.mScaleY * an.mGLHeight, 1.0f);

        camera.pressShutter(shader.getProjectionViewMatrixHandle(), shader.getModelMatrixHandle(), mModelMatrix);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, an.mTextureId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPosVBO.getId());

        GLES20.glVertexAttribPointer(shader.getPosHandle(), 3, GLES20.GL_FLOAT, false, Stride, 0);
        GLES20.glVertexAttribPointer(shader.getTexHandle(), 2, GLES20.GL_FLOAT, false, Stride, TexOffset);

        GLES20.glUniform1f(shader.getAlphaHanler(), an.mCurrentFrame.mAlpha);
        GLES20.glUniform1i(shader.getTextureSampler(), 0);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mDrawListVBO.getId());
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


    public void delete() {
        ceaseFire(true);

        if (null != mDrawListVBO) {
            mDrawListVBO.delete();
            mDrawListVBO = null;
        }

        if (null != mPosVBO) {
            mPosVBO.delete();
            mPosVBO = null;
        }
    }


    /**
     * 弹幕遮罩效果
     */
    public void stencilDraw(BarrageShader mShader, Camera mCamera, GLBulletTrace glAnimation) {
        // 源色将覆盖目标色
//        GLES20.glBlendFunc(GLES20.GL_ONE , GLES20.GL_ZERO );

        GLES20.glBlendFunc(GLES20.GL_ZERO, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        drawFrame(mShader, mCamera, glAnimation);
    }

    /**
     * OpenGL动画元素
     */
    public static class GLBulletTrace extends AbsTrace {

        //弹幕GL高宽，从Bitmap实际大小而来
        public float mGLWidth = 1.0f;
        public float mGLHeight = 1.0f;
        private BulletBuilder.Bullet<ByteBuffer> mBufferBullet;

        public GLBulletTrace(ByteBuffer texture, int width, int height) {
            initAnimation(texture, width, height);
        }

        public GLBulletTrace(Bitmap texture) {
            ByteBuffer pixels = ByteBuffer.allocateDirect(texture.getByteCount());
            pixels.order(ByteOrder.nativeOrder());
            texture.copyPixelsToBuffer(pixels);
            pixels.position(0);
            mUseBitmap = true;
            initAnimation(pixels, texture.getWidth(), texture.getHeight());
        }

        public GLBulletTrace(BulletBuilder.Bullet<ByteBuffer> bullet, int target) {
            mBufferBullet = bullet;
            exploreBullet(bullet);
            mTarget = target;
            initAnimation(bullet.getCacheObject().getContent(), bullet.getPixelsWidth(), bullet.getPixelsHeight());
        }

        public void initAnimation(ByteBuffer texture, int width, int height) {
            mGLWidth = GLCoordinate.toGLUnit(width);
            mGLHeight = GLCoordinate.toGLUnit(height);

            createTexture(texture, width, height);
        }


        private void createTexture(ByteBuffer texture, int width, int height) {
            int[] texId = new int[1];

            GLES20.glGenTextures(1, texId, 0);
            mTextureId = texId[0];

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);

            texture.position(0);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    width,
                    height,
                    0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE,
                    texture);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            CatchError.catchError("GLAnimation create textrue");
        }

        @Override
        public void recycle() {
            int ids[] = new int[1];
            ids[0] = mTextureId;
            GLES20.glDeleteTextures(1, ids, 0);

            CatchError.catchError("GLAnimation delete texture");

            mTextureId = -1;
            if (mBufferBullet != null) {
                mBufferBullet.getCacheObject().decreaseReferenceCount();
            }
        }

        @Override
        public AbsTrace x(float start, float end) {
            mHolds[TraceFrame.X][0] = GLCoordinate.toGLPositionX(start);
            mHolds[TraceFrame.X][1] = GLCoordinate.toGLPositionX(end);
            return this;
        }

        @Override
        public AbsTrace y(float start, float end) {
            mHolds[TraceFrame.Y][0] = GLCoordinate.toGLPositionY(start);
            mHolds[TraceFrame.Y][1] = GLCoordinate.toGLPositionY(end);
            return this;
        }

        public void setGLXY(int x, int y) {
            mCurrentFrame.mX = GLCoordinate.toGLPositionX(x);
            mCurrentFrame.mY = GLCoordinate.toGLPositionY(y);
        }

        public void setGLScaleXY(float scaleX, float scaleY) {
            mCurrentFrame.mScaleX = scaleX;
            mCurrentFrame.mScaleY = scaleY;
        }

    }

    @Override
    public AbsDrawingCache<ByteBuffer> createDrawingCache(Bitmap bitmap) {
        return new AbsDrawingCache.GLDrawingCache(bitmap);
    }
}
