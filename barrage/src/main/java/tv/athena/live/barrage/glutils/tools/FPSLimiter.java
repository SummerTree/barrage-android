package tv.athena.live.barrage.glutils.tools;

import android.os.SystemClock;

import tv.athena.live.barrage.logger.MTPApi;


public class FPSLimiter {

    private long mFPSInterval = 0;
    private long mPreviousFrameTimeStamp = 0;

    private int mFPS = 0;
    private long mLast = 0;
    private long mStart = System.currentTimeMillis();

    public FPSLimiter(int fps) {
        mFPSInterval = 1000 / fps;
    }

    public void limitFPS() {
        long endTime = SystemClock.elapsedRealtime();
        long diffTime = endTime - mPreviousFrameTimeStamp;

        if (diffTime < mFPSInterval) {
            try {
                Thread.sleep(mFPSInterval - diffTime);
            } catch (InterruptedException e) {
                MTPApi.LOGGER.error(this, e);
            }
            mPreviousFrameTimeStamp += mFPSInterval;
        } else {
            mPreviousFrameTimeStamp = endTime;
        }
    }

    public void countFPS() {
        mFPS++;
        if (SystemClock.elapsedRealtime() - mLast >= 1000) {
            if (System.currentTimeMillis() - mStart >= 3000) {
                mStart = System.currentTimeMillis();
                MTPApi.LOGGER.info(this, "fps:%d", mFPS);
            }
            mLast = SystemClock.elapsedRealtime();
            mFPS = 0;
        }
    }


}
