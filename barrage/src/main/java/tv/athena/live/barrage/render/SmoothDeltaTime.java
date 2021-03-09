package tv.athena.live.barrage.render;

import android.os.SystemClock;

import java.util.LinkedList;

/**
 * 弹幕平滑滑动时间计算，避免弹幕突兀
 */
public class SmoothDeltaTime {

    private static final int MAX_RECORD_SIZE = 500;

    private final Timer timer = new Timer();

    /**
     * 弹幕开始时间
     */
    private long mTimeBase;

    private static final long CORDON_TIME = 30;

    private static final long CORDON_TIME_2 = 60;

    private long mLastDeltaTime;

    private static final long FRAME_UPDATE_RATE = 16;

    /**
     * 绘制完成时间
     */
    private LinkedList<Long> mDrawTimes = new LinkedList<>();

    private boolean mInSyncAction;

    public float getSmoothDelta() {
        return timer.lastInterval;
    }

    public long getCurrentTime() {
        return timer.currMillisecond;
    }

    public long calcSmoothDelta() {
        final long startMS = SystemClock.elapsedRealtime();
        if (mInSyncAction) {
            return 0;
        }
        mInSyncAction = true;
        long d;
        // 过去的时间
        long time = startMS - mTimeBase;

        long gapTime = time - timer.currMillisecond;
        long averageTime = Math.max(FRAME_UPDATE_RATE, getAverageRenderingTime());

        d = averageTime + gapTime / FRAME_UPDATE_RATE;
        d = Math.max(FRAME_UPDATE_RATE, d);
        d = Math.min(CORDON_TIME, d);
        long a = d - mLastDeltaTime;
        if (a > 3 && a < 8 && mLastDeltaTime >= FRAME_UPDATE_RATE && mLastDeltaTime <= CORDON_TIME) {
            d = mLastDeltaTime;
        }
        mLastDeltaTime = d;
        timer.add(d);

        mInSyncAction = false;
        return d;
    }

    public synchronized void reset() {
        mDrawTimes.clear();
        mTimeBase = SystemClock.elapsedRealtime();
        timer.currMillisecond = mTimeBase;
    }

    public void start() {
        mTimeBase = SystemClock.elapsedRealtime();
        timer.currMillisecond = mTimeBase;
    }

    /**
     * 平均渲染时间
     */
    private synchronized long getAverageRenderingTime() {
        int frames = mDrawTimes.size();
        if (frames <= 0) {
            return 0;
        }
        Long first = mDrawTimes.peekFirst();
        Long last = mDrawTimes.peekLast();
        if (first == null || last == null) {
            return 0;
        }
        long dtime = last - first;
        return dtime / frames;
    }

    public synchronized void onDrawCost(long d) {
        if (d > CORDON_TIME_2) {
            // this situation may be cuased by ui-thread waiting of DanmakuView, so we sync-timer at once
            timer.add(d);
            mDrawTimes.clear();
        }
    }

    public synchronized void recordRenderingTime() {
        long lastTime = SystemClock.elapsedRealtime();
        mDrawTimes.addLast(lastTime);
        int frames = mDrawTimes.size();
        if (frames > MAX_RECORD_SIZE) {
            mDrawTimes.removeFirst();
        }
    }

    public static class Timer {

        public long currMillisecond;

        private long lastInterval;

        public Timer() {
        }

        public long update(long curr) {
            currMillisecond = curr;
            return lastInterval;
        }

        public long add(long mills) {
            lastInterval = mills;
            return update(currMillisecond + mills);
        }
    }
}