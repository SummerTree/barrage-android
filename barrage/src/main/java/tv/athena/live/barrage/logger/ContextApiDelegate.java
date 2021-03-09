package tv.athena.live.barrage.logger;

import android.app.Application;
import android.content.Context;

/**
 * @author carlosliu on 2019-08-17.
 */
public class ContextApiDelegate implements ContextApi {
    private ContextApi mContextApi;

    public void setContextApi(ContextApi contextApi) {
        mContextApi = contextApi;
    }

    @Override
    public Application getApplication() {
        if (mContextApi != null) {
            return mContextApi.getApplication();
        }
        //TODO: to create a fake application instance
        return null;
    }

    @Override
    public Context getApplicationContext() {
        if (mContextApi != null) {
            return mContextApi.getApplicationContext();
        }
        //TODO: to create a fake application instance
        return null;
    }
}
