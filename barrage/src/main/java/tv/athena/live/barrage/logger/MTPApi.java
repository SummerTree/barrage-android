package tv.athena.live.barrage.logger;


/**
 * @author carlosliu on 2019-08-08.
 */
public class MTPApi {
    public static final DebugApi DEBUGGER = new DebugApiDelegate();
    public static final LogApi LOGGER = new LogApiDelegate();
    public static final ContextApi CONTEXT = new ContextApiDelegate();
    public static final MonitorApi MONITOR = new MonitorApiDelegate();

    public static void setContextApi(ContextApi contextApi) {
        ((ContextApiDelegate) CONTEXT).setContextApi(contextApi);
    }

    public static void setDebugger(DebugApi debugApi) {
        ((DebugApiDelegate) DEBUGGER).setDebugApi(debugApi);
    }

    public static void setLogger(LogApi logApi) {
        ((LogApiDelegate) LOGGER).setLogApi(logApi);
    }

    public static void setMonitorApi(MonitorApi monitorApi) {
        ((MonitorApiDelegate) MONITOR).setMonitorApi(monitorApi);
    }
}
