package tv.athena.live.barrage.glutils;

import android.opengl.GLES20;
import android.util.Log;

public final class KGLGetError {
    private static final String TAG = "KGLGetError";

    public static void debug(String info) {
        int e = GLES20.glGetError();
        if (GLES20.GL_NO_ERROR != e) {
            Log.d(TAG, info + " " + Integer.toHexString(e));
        }
    }

    public static void info(String info) {
        int e = GLES20.glGetError();
        if (GLES20.GL_NO_ERROR != e) {
            Log.d(TAG, info + " " + Integer.toHexString(e));
        }
    }

    public static void error(String info) {
        int e = GLES20.glGetError();
        if (GLES20.GL_NO_ERROR != e) {
            Log.e(TAG, info + " " + Integer.toHexString(e));
        }
    }
}
