package tv.athena.live.barrage.glutils;

import tv.athena.live.barrage.glutils.tools.KGLConstant;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class KGLUtils {
    public static float[] arrayCopy(float[] array) {
        float[] cp = new float[array.length];
        System.arraycopy(array, 0, cp, 0, array.length);
        return cp;
    }

    public static FloatBuffer arrayToBuffer(float[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * KGLConstant.SizeFloat);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(array);
        fb.position(0);
        return fb;
    }

    public static ShortBuffer arrayToBuffer(short[] array) {
        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * KGLConstant.SizeShort);
        bb.order(ByteOrder.nativeOrder());

        ShortBuffer sb = bb.asShortBuffer();
        sb.put(array);
        sb.position(0);
        return sb;
    }
}
