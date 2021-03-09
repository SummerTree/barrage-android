package tv.athena.live.barrage.stencil;

import android.text.TextUtils;

import tv.athena.live.barrage.config.BarrageLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tv.athena.live.barrage.logger.MTPApi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by QiuXi'an on 2018/9/14.
 */
public class StencilConfig {
    private static final String TAG = StencilConfig.class.getSimpleName();
    private List<String> mGameIdList = new ArrayList<>();
    private static StencilConfig sInstance = new StencilConfig();

    private StencilConfig() {
    }

    public static StencilConfig getInstance() {
        return sInstance;
    }

    public synchronized boolean isStencilAble(String id) {
        for (int i = 0; i < mGameIdList.size(); i++) {
            if (mGameIdList.get(i).equals(id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void setGameIdList(String str) {
        mGameIdList.clear();
        if (TextUtils.isEmpty(str)) {
            return;
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            mGameIdList.addAll((List<String>) gson.fromJson(str, type));
            BarrageLog.debug(TAG, "onDynamicConfig  = %s", Arrays.toString(mGameIdList.toArray()));
        } catch (Exception e) {
            BarrageLog.error(TAG, "parse gameid list error", e);
            MTPApi.DEBUGGER.crashIfDebug(TAG, e);
        }
    }
}
