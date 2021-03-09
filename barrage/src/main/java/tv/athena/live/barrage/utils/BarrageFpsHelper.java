package tv.athena.live.barrage.utils;

import android.os.SystemClock;

import java.util.LinkedList;
import java.util.Locale;

/**
 * @author mylhyz 2018/12/20
 */
public class BarrageFpsHelper {

    public static BarrageFpsHelper create() {
        return new BarrageFpsHelper();
    }

    private LinkedList<Long> mDrawTimes = new LinkedList<>();
    private static final int MAX_RECORD_SIZE = 50;
    private static final int ONE_SECOND = 1000;
    private long mLastTime;

    public void update() {
        mLastTime = SystemClock.uptimeMillis();
        mDrawTimes.addLast(mLastTime);
    }

    public float fps() {
        Long first = mDrawTimes.peekFirst();
        if (first == null) {
            return 0.0f;
        }
        float dtime = mLastTime - first;
        int frames = mDrawTimes.size();
        if (frames > MAX_RECORD_SIZE) {
            mDrawTimes.removeFirst();
        }
        return dtime > 0 ? mDrawTimes.size() * ONE_SECOND / dtime : 0.0f;
    }


    public String getFpsStr() {
        return String.format(Locale.getDefault(),
                "fps %.2f", fps());
    }
}
