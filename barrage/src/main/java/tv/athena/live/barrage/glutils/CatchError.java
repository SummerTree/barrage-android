package tv.athena.live.barrage.glutils;

import android.opengl.GLES20;

import tv.athena.live.barrage.logger.MTPApi;


public class CatchError {
    public static void catchError(String line) {
        int a = GLES20.glGetError();
        if (GLES20.GL_NO_ERROR != a) {
            MTPApi.LOGGER.error("CatchError", line + "  " + a);
        }
    }
}
