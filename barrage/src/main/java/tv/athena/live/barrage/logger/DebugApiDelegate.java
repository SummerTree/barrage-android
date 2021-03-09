package tv.athena.live.barrage.logger;

import androidx.annotation.Nullable;

/**
 * @author carlosliu
 */
public class DebugApiDelegate implements DebugApi {
    private DebugApi mDebugApi;

    public DebugApiDelegate() {
        this(null);
    }

    public DebugApiDelegate(@Nullable DebugApi debugApi) {
        mDebugApi = debugApi;
    }

    public void setDebugApi(DebugApi debugApi) {
        mDebugApi = debugApi;
    }

    @Override
    public void crashIfDebug(String format, Object... args) {
        if (mDebugApi != null) {
            mDebugApi.crashIfDebug(format, args);
        }
    }

    @Override
    public void crashIfDebug(Throwable cause, String format, Object... args) {
        if (mDebugApi != null) {
            mDebugApi.crashIfDebug(cause, format, args);
        }
    }

    @Override
    public void crashIfDebug(boolean condition, String format, Object... args) {
        if (mDebugApi != null) {
            mDebugApi.crashIfDebug(condition, format, args);
        }
    }
}
