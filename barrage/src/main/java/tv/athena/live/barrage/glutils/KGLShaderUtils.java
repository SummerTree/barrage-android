package tv.athena.live.barrage.glutils;

import android.opengl.GLES20;
import android.util.Log;

public final class KGLShaderUtils {
    public enum ShaderType {
        Vertex(GLES20.GL_VERTEX_SHADER),
        Fragment(GLES20.GL_FRAGMENT_SHADER);

        private int mType;

        ShaderType(int type) {
            mType = type;
        }
    }

    public static int compileShader(ShaderType shader, final String shaderCode) {
        int shaderHandle = GLES20.glCreateShader(shader.mType);
        KGLGetError.info("compile shader step 1");

        GLES20.glShaderSource(shaderHandle, shaderCode);
        KGLGetError.info("compile shader step 2");

        GLES20.glCompileShader(shaderHandle);
        KGLGetError.info("compile shader step 3");

        int status = checkShaderStatus(shaderHandle, GLES20.GL_COMPILE_STATUS);
        KGLGetError.info("compile shader step 4");

        if (GLES20.GL_FALSE == status) {
            GLES20.glDeleteShader(shaderHandle);
            KGLGetError.info("compile shader step 5");

            return GLES20.GL_FALSE;
        }

        return shaderHandle;
    }

    public static int linkShader(int... shaderList) {
        int program = GLES20.glCreateProgram();

        for (int shader : shaderList) {
            GLES20.glAttachShader(program, shader);
            KGLGetError.error("link shader error " + shader);
        }

        GLES20.glLinkProgram(program);

        int status = checkProgramStatus(program, GLES20.GL_LINK_STATUS);
        if (GLES20.GL_FALSE == status) {
            deleteShaderProgram(program, shaderList);
            return GLES20.GL_FALSE;
        }

        return program;
    }

    public static int validateShaderProgram(int program) {
        GLES20.glValidateProgram(program);

        return checkProgramStatus(program, GLES20.GL_VALIDATE_STATUS);
    }

    public static int deleteShaderProgram(int program, int... shaderList) {
        for (int shader : shaderList) {
            if (GLES20.glIsShader(shader)) {
                GLES20.glDetachShader(program, shader);
                GLES20.glDeleteShader(shader);
                KGLGetError.error("delete shader error " + program + " shader " + shader);

                if (GLES20.GL_FALSE == checkShaderStatus(shader, GLES20.GL_DELETE_STATUS)) {
                    KGLGetError.error("delete shader chech status error " + program + " shader " + shader);
                }
            }
        }

        if (GLES20.glIsProgram(program)) {
            GLES20.glDeleteProgram(program);
            if (GLES20.GL_FALSE == checkProgramStatus(program, GLES20.GL_DELETE_STATUS)) {
                return GLES20.GL_FALSE;
            }
        }

        return GLES20.GL_TRUE;
    }

    private static int checkProgramStatus(int program, int programStatus) {
        int[] status = {GLES20.GL_TRUE};
        GLES20.glGetProgramiv(program, programStatus, status, 0);

        if (GLES20.GL_TRUE == status[0]) {
            return GLES20.GL_TRUE;
        }

        Log.e("checkProgramStatus", GLES20.glGetProgramInfoLog(program) + " " + status[0]);
        return GLES20.GL_FALSE;
    }

    private static int checkShaderStatus(int shader, int shaderStatus) {
        int[] status = {GLES20.GL_TRUE};
        GLES20.glGetShaderiv(shader, shaderStatus, status, 0);

        if (GLES20.GL_TRUE == status[0]) {
            return GLES20.GL_TRUE;
        }

        Log.e("checkShaderStatus", GLES20.glGetShaderInfoLog(shader) + " " + status[0]);
        return GLES20.GL_FALSE;
    }
}
