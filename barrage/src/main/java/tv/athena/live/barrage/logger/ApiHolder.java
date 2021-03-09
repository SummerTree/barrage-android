package tv.athena.live.barrage.logger;

/**
 * @author carlosliu on 2019/1/16.
 */
public class ApiHolder {
    private DebugApi mDebugApi;
    private LogApi mLogApi;

    public DebugApi getDebugApi() {
        return mDebugApi;
    }

    public void setDebugApi(DebugApi debugApi) {
        mDebugApi = debugApi;
    }

    public LogApi getLogApi() {
        return mLogApi;
    }

    public void setLogApi(LogApi logApi) {
        mLogApi = logApi;
    }
}
