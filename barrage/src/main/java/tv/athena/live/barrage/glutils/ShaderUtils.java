package tv.athena.live.barrage.glutils;

import android.opengl.GLES20;

import tv.athena.live.barrage.logger.MTPApi;

public class ShaderUtils {
    public static int compileShader(int type, final String shaderCode) {
        CatchError.catchError("compileShader0");

        int shaderHandle = GLES20.glCreateShader(type);
        CatchError.catchError("compileShader1");

        GLES20.glShaderSource(shaderHandle, shaderCode);
        CatchError.catchError("compileShader2");

        GLES20.glCompileShader(shaderHandle);
        CatchError.catchError("compileShader3");

        int status = checkShaderStatus(shaderHandle, GLES20.GL_COMPILE_STATUS);
        CatchError.catchError("compileShader4");

        if (GLES20.GL_FALSE == status) {
            GLES20.glDeleteShader(shaderHandle);
            CatchError.catchError("compileShader5");

            return GLES20.GL_FALSE;
        }

        return shaderHandle;
    }

    public static int linkShader(int... shaderList) {
        int program = GLES20.glCreateProgram();

        for (int shader : shaderList) {
            GLES20.glAttachShader(program, shader);
            CatchError.catchError("linkShader 0 shader " + shader);
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
                CatchError.catchError("deleteShaderProgram program " + program + " shader " + shader);

                if (GLES20.GL_FALSE == checkShaderStatus(shader, GLES20.GL_DELETE_STATUS)) {
                    CatchError.catchError("deleteShaderProgram check status " + program + " shader " + shader);
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
        CatchError.catchError("checkProgramStatus");

        if (GLES20.GL_TRUE == status[0]) {
            return GLES20.GL_TRUE;
        }

        MTPApi.LOGGER.error("checkProgramStatus", GLES20.glGetProgramInfoLog(program) + " " + status[0]);
        CatchError.catchError("checkProgramStatus1");
        return GLES20.GL_FALSE;
    }

    private static int checkShaderStatus(int shader, int shaderStatus) {
        int[] status = {GLES20.GL_TRUE};
        GLES20.glGetShaderiv(shader, shaderStatus, status, 0);
        CatchError.catchError("checkShaderStatus");

        if (GLES20.GL_TRUE == status[0]) {
            return GLES20.GL_TRUE;
        }

        MTPApi.LOGGER.error("checkShaderStatus", GLES20.glGetShaderInfoLog(shader) + " " + status[0]);
        CatchError.catchError("checkShaderStatus1");
        return GLES20.GL_FALSE;
    }
}
