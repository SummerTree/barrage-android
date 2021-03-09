package tv.athena.live.barrage.config;

import android.os.Build;

/**
 * @author dh on 2018/6/29.
 */
public class GLBarrageAdapter {

    // 魅族7.0手机OpenGL有一个崩溃，以下手机需要特殊处理，虽然魅族已经修复，但是保险起见，我们还是对这几个机型采取特殊保护
    // 首先Flyme 6.2.0.0A （编译id给用户看的)魅族手机才会有
    // Meizu M5 Note	Android 7.0	337
    // Meizu M1 E	Android 7.0	63
    // Meizu M3 Max	Android 7.0	16
    // Meizu m3 note	Android 7.0	4


    public static boolean isOpenGLBarrageProblemSystem() {
        return sIsOpenGLBarrageProblemSystem;
    }

    //另外一些手机有onStop调用较晚的情况，P20和Vivo X21，导致系统没有及时清理surface残影，而导致各种弹幕残影保留问题，
    //这里只能保证在onPause时机清理弹幕
    private static boolean sIsOpenGLBarrageProblemSystem = false;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sIsOpenGLBarrageProblemSystem = true;
        } else {
            // board.toLowerCase().equals("sdm660")
            String board = Build.BOARD;
            if (board != null) {
                board = board.toLowerCase();
                if (board.equals("m5 note")
                        || board.equals("m1 e")
                        || board.equals("m3 max")
                        || board.toLowerCase().equals("m3 note")) {
                    sIsOpenGLBarrageProblemSystem = true;
                }
            }
        }
    }


    /**
     * 弹幕只做清理动作
     */
    private static volatile boolean sNeedCleanOnPase = false;
    /**
     * 弹幕是否开启开关
     */
    private static volatile boolean sIsBarrageRenderOn = false;
    /**
     *
     */
    private static volatile boolean sHasClean = true;

    public static void pause() {
        BarrageLog.error("wolf", "enter pause------");

        if (sIsOpenGLBarrageProblemSystem && isRenderOn()) {
            sNeedCleanOnPase = true;
            sHasClean = false;
        }
    }

    public static void resume() {
        if (sIsOpenGLBarrageProblemSystem) {
            sNeedCleanOnPase = false;
            sHasClean = true;
        }
    }

    public static boolean needClear() {
        return sNeedCleanOnPase;
    }

    public static boolean hasClean() {
        return sHasClean;
    }

    public static void setHasClean() {
        sHasClean = true;
    }

    public static void setRenderOn(boolean isRenderOn) {
        sIsBarrageRenderOn = isRenderOn;
    }

    private static boolean isRenderOn() {
        return sIsBarrageRenderOn;
    }
}
