package tv.athena.live.barrage.logger;


import androidx.annotation.Nullable;

/**
 * @author carlosliu
 */
public class LogApiDelegate implements LogApi {
    private LogApi mLogApi;

    public LogApiDelegate() {
    }

    public LogApiDelegate(@Nullable LogApi logApi) {
        mLogApi = logApi;
    }

    public void setLogApi(LogApi logApi) {
        mLogApi = logApi;
    }

    public void verbose(String msg) {
        if (mLogApi != null) {
            mLogApi.verbose(msg);
        }
    }

    public void verbose(Object tag, String format) {
        if (mLogApi != null) {
            mLogApi.verbose(tag, format);
        }
    }

    public void verbose(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.verbose(tag, format, args);
        }
    }

    public void verbose(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.verbose(tag, message, t);
        }
    }

    public void verbose(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.verbose(tag, t);
        }
    }

    public void debug(String msg) {
        if (mLogApi != null) {
            mLogApi.debug(msg);
        }
    }

    public void debug(Object tag, String message) {
        if (mLogApi != null) {
            mLogApi.debug(tag, message);
        }
    }

    public void debug(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.debug(tag, format, args);
        }
    }

    public void debug(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.debug(tag, message, t);
        }
    }

    public void debug(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.debug(tag, t);
        }
    }

    public void info(String msg) {
        if (mLogApi != null) {
            mLogApi.info(msg);
        }
    }

    public void info(Object tag, String format) {
        if (mLogApi != null) {
            mLogApi.info(tag, format);
        }
    }

    public void info(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.info(tag, format, args);
        }
    }

    public void info(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.info(tag, message, t);
        }
    }

    public void info(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.info(tag, t);
        }
    }

    public void warn(String msg) {
        if (mLogApi != null) {
            mLogApi.warn(msg);
        }
    }

    public void warn(Object tag, String message) {
        if (mLogApi != null) {
            mLogApi.warn(tag, message);
        }
    }

    public void warn(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.warn(tag, format, args);
        }
    }

    public void warn(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.warn(tag, message, t);
        }
    }

    public void warn(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.warn(tag, t);
        }
    }

    public void error(String msg) {
        if (mLogApi != null) {
            mLogApi.error(msg);
        }
    }

    public void error(Object tag, String message) {
        if (mLogApi != null) {
            mLogApi.error(tag, message);
        }
    }

    public void error(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.error(tag, format, args);
        }
    }

    public void error(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.error(tag, message, t);
        }
    }

    public void error(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.error(tag, t);
        }
    }

    public void fatal(String msg) {
        if (mLogApi != null) {
            mLogApi.fatal(msg);
        }
    }

    public void fatal(Object tag, String message) {
        if (mLogApi != null) {
            mLogApi.fatal(tag, message);
        }
    }

    public void fatal(Object tag, String format, Object... args) {
        if (mLogApi != null) {
            mLogApi.fatal(tag, format, args);
        }
    }

    public void fatal(Object tag, String message, Throwable t) {
        if (mLogApi != null) {
            mLogApi.fatal(tag, message, t);
        }
    }

    public void fatal(Object tag, Throwable t) {
        if (mLogApi != null) {
            mLogApi.fatal(tag, t);
        }
    }

    public void uncaughtException(Throwable t) {
        if (mLogApi != null) {
            mLogApi.uncaughtException(t);
        }
    }

    public void flushToDisk() {
        if (mLogApi != null) {
            mLogApi.flushToDisk();
        }
    }

    @Override
    public boolean isLogLevelEnabled(int level) {
        if (mLogApi != null) {
            return mLogApi.isLogLevelEnabled(level);
        }
        return false;
    }

}
