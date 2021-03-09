package tv.athena.live.barrage.logger;

/**
 * 测试工具接口
 *
 * @author carlosliu
 */
public interface DebugApi {
    /**
     * 辅助工具，如果是测试环境则崩溃
     */
    void crashIfDebug(String format, Object... args);

    /**
     * 辅助工具，如果是测试环境则崩溃
     */
    void crashIfDebug(Throwable cause, String format, Object... args);

    /**
     * 辅助工具，如果是测试环境则崩溃
     */
    void crashIfDebug(boolean condition, String format, Object... args);
}
