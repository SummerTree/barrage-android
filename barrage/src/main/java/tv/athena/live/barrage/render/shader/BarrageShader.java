package tv.athena.live.barrage.render.shader;

import android.opengl.GLES20;

import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.glutils.CatchError;
import tv.athena.live.barrage.glutils.ShaderUtils;


public class BarrageShader {

    protected int mProgram;
    protected int mPositionHandle;
    protected int mTextureHandle;
    protected int mProjectionViewMatrixHandle;
    protected int mModelMatrixHandle;
    protected int mTextureSampler;
    protected int mAlphaHanler;

    protected int mVertexShaderHandle;
    protected int mFragmentShaderHandle;


    public BarrageShader() {
        init();
    }

    public void use() {
        GLES20.glUseProgram(mProgram);
        CatchError.catchError("shader use1");
    }

    public void unUse() {
        GLES20.glUseProgram(0);
    }

    public void destroy() {
        ShaderUtils.deleteShaderProgram(mProgram, mVertexShaderHandle, mFragmentShaderHandle);

        mProgram = -1;
        mPositionHandle = -1;
        mTextureHandle = -1;
        mProjectionViewMatrixHandle = -1;
        mModelMatrixHandle = -1;
        mVertexShaderHandle = -1;
        mFragmentShaderHandle = -1;
        mTextureSampler = -1;
        mAlphaHanler = -1;
    }

    public int getPosHandle() {
        return mPositionHandle;
    }

    public int getTexHandle() {
        return mTextureHandle;
    }

    public int getProjectionViewMatrixHandle() {
        return mProjectionViewMatrixHandle;
    }

    public int getModelMatrixHandle() {
        return mModelMatrixHandle;
    }

    public int getTextureSampler() {
        return mTextureSampler;
    }

    public int getAlphaHanler() {
        return mAlphaHanler;
    }

    private void init() {
        mVertexShaderHandle = ShaderUtils.compileShader(GLES20.GL_VERTEX_SHADER, ShaderCode.VSH_CODE);
        CatchError.catchError("barrage shader init 1");

        mFragmentShaderHandle = ShaderUtils.compileShader(GLES20.GL_FRAGMENT_SHADER, ShaderCode.FSH_CODE);
        CatchError.catchError("barrage shader init 2");

        mProgram = ShaderUtils.linkShader(mVertexShaderHandle, mFragmentShaderHandle);
        CatchError.catchError("barrage shader init 3");

        ShaderUtils.validateShaderProgram(mProgram);
        CatchError.catchError("barrage shader init 4");

        if (GLES20.GL_FALSE == mVertexShaderHandle || GLES20.GL_FALSE == mFragmentShaderHandle
                || GLES20.GL_FALSE == mProgram) {
            BarrageLog.error("CatchError", "barrage shader init error vsh %d fsh %d program %d",
                    mVertexShaderHandle, mFragmentShaderHandle, mProgram);
            mVertexShaderHandle = mFragmentShaderHandle = mProgram = -1;
            return;
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, ShaderCode.POSITION);
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, ShaderCode.TEXTURE);
        mProjectionViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, ShaderCode.PROJECTION_VIEW_MATRIX);
        mModelMatrixHandle = GLES20.glGetUniformLocation(mProgram, ShaderCode.MODEL_MATRIX);
        mTextureSampler = GLES20.glGetUniformLocation(mProgram, ShaderCode.SAMPLER);
        mAlphaHanler = GLES20.glGetUniformLocation(mProgram, ShaderCode.ALPHA);
        CatchError.catchError("barrage shader use2");
    }
}
