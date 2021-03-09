package tv.athena.live.barrage.glutils.tools;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Camera {
    private float[] mFrustumMatrix = null;
    private float[] mLookAtMatrix = null;
    private float[] mCurrentMatrix = null;

    private float mTop;
    private float mBottom;
    private float mNear;
    private float mFar;
    private float mEyeZ;

    public Camera(float top, float bottom, float near, float far, float eyeZ) {
        mFrustumMatrix = new float[16];
        mLookAtMatrix = new float[16];
        mCurrentMatrix = new float[16];

        Matrix.setIdentityM(mFrustumMatrix, 0);
        Matrix.setIdentityM(mLookAtMatrix, 0);
        Matrix.setIdentityM(mCurrentMatrix, 0);

        mTop = top;
        mBottom = bottom;
        mNear = near;
        mFar = far;
        mEyeZ = eyeZ;
    }

    public void sharpFocusing(float left, float right) {
        Matrix.frustumM(mFrustumMatrix, 0, left, right, mBottom, mTop, mNear, mFar);
        Matrix.multiplyMM(mCurrentMatrix, 0, mFrustumMatrix, 0, mLookAtMatrix, 0);
    }

    public void setUp() {
        Matrix.setLookAtM(mLookAtMatrix, 0, 0.0f, 0.0f, mEyeZ, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f, 0.0f);
        Matrix.multiplyMM(mCurrentMatrix, 0, mFrustumMatrix, 0, mLookAtMatrix, 0);
    }

    public void pressShutter(int projectionViewMatrixHandle, int modelMatrixHandle, float[] modelMatrix) {
        GLES20.glUniformMatrix4fv(projectionViewMatrixHandle, 1, false, mCurrentMatrix, 0);
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);
    }
}
