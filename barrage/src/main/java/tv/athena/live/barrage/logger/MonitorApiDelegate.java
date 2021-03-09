package tv.athena.live.barrage.logger;

import com.huya.data.MonitorReqData;

/**
 * @author Acropolis
 */
public class MonitorApiDelegate implements MonitorApi {

    private MonitorApi mMonitorApi;

    public void setMonitorApi(MonitorApi monitorApi) {
        mMonitorApi = monitorApi;
    }

    @Override
    public void execute(Runnable runnable) {
        if (mMonitorApi != null) {
            mMonitorApi.execute(runnable);
        }
    }

    @Override
    public void executeDelayed(Runnable runnable, long delayMillis) {
        if (mMonitorApi != null) {
            mMonitorApi.executeDelayed(runnable, delayMillis);
        }
    }

    @Override
    public void request(MonitorReqData reqData) {
        if (mMonitorApi != null) {
            mMonitorApi.request(reqData);
        }
    }
}
