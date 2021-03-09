package tv.athena.live.barrage.logger;


import android.app.Application;
import android.content.Context;

/**
 * @author carlosliu on 2019-08-17.
 */
public interface ContextApi {
    Application getApplication();

    Context getApplicationContext();
}


