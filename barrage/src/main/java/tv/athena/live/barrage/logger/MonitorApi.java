package tv.athena.live.barrage.logger;

import com.huya.data.MonitorReqData;

/**
 * @author Acropolis
 */
public interface MonitorApi {

    void execute(Runnable runnable);

    void executeDelayed(Runnable runnable, long delayMillis);

    void request(MonitorReqData reqData);
}
