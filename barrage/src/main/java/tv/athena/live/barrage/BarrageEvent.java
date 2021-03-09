package tv.athena.live.barrage;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * 弹幕相关事件
 *
 * @author dh on 2017/5/4.
 */

public class BarrageEvent {

    //别人的弹幕
    public static class PubText {
        public ArrayList<String> mTestList;
        public int mDefColor;
        public int mSpeedMode;

        public PubText(ArrayList powder, int defColor, int speedMode) {
            mTestList = powder;
            mDefColor = defColor;
            mSpeedMode = speedMode;
        }
    }

    //自己发的弹幕
    public static class TextAboutToSendV2 {
        public String mContent;
        public Integer mBulletColor;
        public String arg3; //暂时无用，命令行用

        public TextAboutToSendV2(String content, Integer bulletColor, String arg3) {
            this.mContent = content;
            this.mBulletColor = bulletColor;
            this.arg3 = arg3;
        }
    }//content, barrageColor,bulletColor xxBarrage cmd


    public static class BarrageAlphaChanged {
        public Float arg0;

        public BarrageAlphaChanged(Float arg0) {
            this.arg0 = arg0;
        }
    }

    public static class BarrageSizeChanged {
        public Integer arg0;

        public BarrageSizeChanged(Integer arg0) {
            this.arg0 = arg0;
        }
    }

    public static class BarrageModelChanged {
        public int mode;

        public BarrageModelChanged(int mode) {
            this.mode = mode;
        }
    }


    //清屏
    public static class CleanVideoBarrage {

    }

    //暂停
    public static class PauseVideoBarrage {

    }

    //恢复
    public static class ResumeVideoBarrage {

    }

    //包含yy表情的弹幕
    public static class BarrageWithFace {
        public GunPowder mGunPowder;

        public BarrageWithFace(GunPowder gunPowder) {
            mGunPowder = gunPowder;
        }
    }

    //有附件的弹幕
    public static class BarrageWithAttach {
        public Object mAttach;

        public BarrageWithAttach(Object object) {
            mAttach = object;
        }
    }

    /**
     * 跑马灯弹幕
     */
    public static class RequireMarqueeInSurface {
        public Bitmap bitmap;
        public long duration;

        public RequireMarqueeInSurface(Bitmap bitmap, long duration) {
            this.bitmap = bitmap;
            this.duration = duration;
        }
    }

    /**
     * 显示防弹幕遮罩提示
     */
    public static class ShowAntiBlockTip {

    }
}
