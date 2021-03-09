package tv.athena.live.barrage.logger;

/**
 * 日志接口类，提供不同等级的日志打印接口
 *
 * @author carlosliu
 */
public interface LogApi {
    void verbose(String msg);

    void verbose(Object tag, String format);

    void verbose(Object tag, String format, Object... args);

    void verbose(Object tag, String message, Throwable t);

    void verbose(Object tag, Throwable t);

    void debug(String msg);

    void debug(Object tag, String message);

    void debug(Object tag, String format, Object... args);

    void debug(Object tag, String message, Throwable t);

    void debug(Object tag, Throwable t);

    void info(String msg);

    void info(Object tag, String format);

    void info(Object tag, String format, Object... args);

    void info(Object tag, String message, Throwable t);

    void info(Object tag, Throwable t);

    void warn(String msg);

    void warn(Object tag, String message);

    void warn(Object tag, String format, Object... args);

    void warn(Object tag, String message, Throwable t);

    void warn(Object tag, Throwable t);

    void error(String msg);

    void error(Object tag, String message);

    void error(Object tag, String format, Object... args);

    void error(Object tag, String message, Throwable t);

    void error(Object tag, Throwable t);

    void fatal(String msg);

    void fatal(Object tag, String message);

    void fatal(Object tag, String format, Object... args);

    void fatal(Object tag, String message, Throwable t);

    void fatal(Object tag, Throwable t);

    void uncaughtException(Throwable t);

    void flushToDisk();

    boolean isLogLevelEnabled(int level);
}
