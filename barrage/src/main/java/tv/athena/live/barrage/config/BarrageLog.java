package tv.athena.live.barrage.config;

import android.os.Process;
import android.util.Log;

/**
 * @author dh on 2019/5/8.
 * <p>
 * 弹幕库日志系统，如果需要自定义，请实现IBarrageLog接口，且初始化app的时候调用setBarrageLog方法
 */
public class BarrageLog {

    private static IBarrageLog sBarrageLogProxy = new DefaultLog();

    public static void setBarrageLog(IBarrageLog iBarrageLog) {
        if (iBarrageLog != null) {
            sBarrageLogProxy = iBarrageLog;
        }
    }

    public static void debug(String tag, String message) {
        sBarrageLogProxy.debug(tag, message);
    }

    public static void debug(String tag, String format, Object... args) {
        sBarrageLogProxy.debug(tag, format, args);
    }

    public static void info(String tag, String format) {
        sBarrageLogProxy.info(tag, format);
    }

    public static void info(String tag, String format, Object... args) {
        sBarrageLogProxy.info(tag, format, args);
    }


    public static void error(String tag, String message) {
        sBarrageLogProxy.error(tag, message);
    }

    public static void error(String tag, String format, Object... args) {
        sBarrageLogProxy.error(tag, format, args);
    }

    public static void error(String tag, String message, Throwable t) {
        sBarrageLogProxy.error(tag, message, t);
    }


    public static class DefaultLog implements IBarrageLog {

        private static int sPid = 0;

        @Override
        public void debug(String tag, String message) {
            doLog(Log.DEBUG, tag, message, null, true);
        }

        @Override
        public void debug(String tag, String format, Object... args) {
            doLog(Log.DEBUG, tag, String.format(format, args), null, true);
        }

        @Override
        public void info(String tag, String format) {
            doLog(Log.INFO, tag, format, null, true);
        }

        @Override
        public void info(String tag, String format, Object... args) {
            doLog(Log.INFO, tag, String.format(format, args), null, true);
        }

        @Override
        public void error(String tag, String message) {
            doLog(Log.ERROR, tag, message, null, true);
        }

        @Override
        public void error(String tag, String format, Object... args) {
            doLog(Log.ERROR, tag, String.format(format, args), null, true);
        }

        @Override
        public void error(String tag, String message, Throwable t) {
            doLog(Log.ERROR, tag, message, t, true);
        }

        private static void doLog(final int logLevel, String tag, final String message, final Throwable t, boolean needStackTrace) {
            String msg;
            if (needStackTrace) {
                msg = getLogInfo(tag, message, t);
            } else {
                msg = getLogInfo(tag, message, t, false);
            }

            logByLevelReal(logLevel, msg, tag);
        }

        static void logByLevelReal(int type, String msg, String TAG) {
            switch (type) {
                case Log.VERBOSE: {
                    Log.v(TAG, msg);
                    break;
                }
                case Log.DEBUG: {
                    Log.d(TAG, msg);
                    break;
                }
                case Log.INFO: {
                    Log.i(TAG, msg);
                    break;
                }
                case Log.WARN: {
                    Log.w(TAG, msg);
                    break;
                }
                case Log.ERROR: {
                    Log.e(TAG, msg);
                    break;
                }
            }
        }

        /**
         * 获取详细日志信息，可能有耗时操作
         *
         * @param tag            tag
         * @param message        message
         * @param t              throwable
         * @param needStackTrace 需要打印栈
         * @return 具体日志字符串
         */
        private static String getLogInfo(String tag, String message, Throwable t, boolean needStackTrace) {
            String fileName = "";
            int lineNumber = 0;
            if (needStackTrace) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StackTraceElement element = null;
                if (stackTraceElements != null && stackTraceElements.length > 5) {
                    element = stackTraceElements[5];
                }
                if (element != null) {
                    fileName = element.getFileName();
                    lineNumber = element.getLineNumber();
                }
            }
            return msgForTextLog(tag, fileName, lineNumber, message, t, needStackTrace);
        }

        private static String msgForTextLog(String tag, String filename, int line,
                                            String msg, Throwable t, boolean needStackTrace) {
            StringBuilder sb = new StringBuilder(64);
            sb.append(msg);
            if (0 == sPid) {
                sPid = Process.myPid();
            }
            sb.append("(P:").append(sPid).append(")");
            sb.append("(T:").append(Thread.currentThread().getName()).append("-").append(Process.myTid()).append(")");
            sb.append("(C:").append(tag).append(")");
            if (needStackTrace) {
                sb.append("at (").append(filename).append(":").append(line).append(")");
            }
            if (t != null) {
                sb.append('\n').append(Log.getStackTraceString(t));
            }
            return sb.toString();
        }

        private static String getLogInfo(String tag, String message, Throwable t) {
            if (0 == sPid) {
                sPid = Process.myPid();
            }

            StringBuilder sb = new StringBuilder(64);
            sb.append(message);
            sb.append("(P:").append(sPid).append(")");
            sb.append("(T:").append(Thread.currentThread().getName()).append("-").append(Process.myTid()).append(")");
            sb.append("(C:").append(objClassName(tag)).append(")");
            if (t != null) {
                sb.append('\n').append(Log.getStackTraceString(t));
            }
            return sb.toString();
        }

        private static String objClassName(Object obj) {
            if (obj == null) {
                return "Global";
            }
            if (obj instanceof String) {
                return (String) obj;
            } else if (obj instanceof Class) {
                return ((Class) obj).getSimpleName();
            } else {
                return obj.getClass().getSimpleName();
            }
        }
    }

    public interface IBarrageLog {

        void debug(String tag, String message);

        void debug(String tag, String format, Object... args);

        void info(String tag, String message);

        void info(String tag, String format, Object... args);

        void error(String tag, String message);

        void error(String tag, String format, Object... args);

        void error(String tag, String message, Throwable t);
    }

}
