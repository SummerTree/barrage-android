package tv.athena.live.barrage;

import android.content.Context;

import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;

/**
 * @author dh on 2019/5/8.
 * <p>
 * 直播间初始化类，需要在app启动之时设置Context和最定义log等
 */
public class BarrageIniter {

    public static void init(Context context, BarrageLog.IBarrageLog barrageLog) {
        BarrageContext.gContext = context;
        BarrageLog.setBarrageLog(barrageLog);
    }

}
