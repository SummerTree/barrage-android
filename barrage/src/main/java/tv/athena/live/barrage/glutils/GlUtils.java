package tv.athena.live.barrage.glutils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * @author carlosliu on 2017/8/17.
 */

public class GlUtils {
    public static String getOpenGLVersion(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int ver = am.getDeviceConfigurationInfo().reqGlEsVersion;
        int h = (ver & 0xffff0000) >> 16;
        int l = ver & 0x0000ffff;
        return String.valueOf(h) + "." + String.valueOf(l);
    }
}
