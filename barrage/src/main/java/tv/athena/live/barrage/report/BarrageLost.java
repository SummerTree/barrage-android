package tv.athena.live.barrage.report;

/**
 * 弹幕丢失统计
 *
 * @author dh on 2018/8/22.
 */

public class BarrageLost {

    /**
     * 统计上报丢弃弹幕次数
     **/
    private static int sBarrageLostTimes = 0;

    public static void recordLostBarrage() {
        sBarrageLostTimes++;
    }

    public static void clearLostBarrage() {
        sBarrageLostTimes = 0;
    }

    public static int getLastBarrageTimes() {
        return sBarrageLostTimes;
    }

}
