package tv.athena.live.barrage.trace;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.area.AnimationListenerImpl;
import tv.athena.live.barrage.render.area.OnAnimationListener;
import tv.athena.live.barrage.render.draw.BulletBuilder;

/**
 * 子弹轨迹，弹幕基础动画属性
 *
 * @author donghao  on 2018/8/28.
 */

public abstract class AbsTrace {

    public static final int RESTART = 0;
    public static final int REVERSE = 1;

    public static final int INFINITE = -1;

    private static final int START = 0;
    private static final int END = 1;

    public float mDuration = 0.0f;
    public float mCurrentTime = 0.0f;
    public float mBeginTime = 0.0f;

    public int mRepeated = 0;
    public int mRepeatCount = 0;
    public int mRepeatModel = RESTART;

    public int mTextureId = -1;

    public boolean mIsOwn = false;

    protected boolean mUseBitmap = false;

    public OnAnimationListener mListener;

    public final float[][] mHolds = new float[5][2];
    public float[] mSpeeds = new float[5];
    public TraceFrame mCurrentFrame = new TraceFrame();

    //发言者uid和内容
    public long mUid = 0;
    public String mNickName;
    public String mText;
    public long mCurrentTimeMillis = System.currentTimeMillis();
    public int mExplosive;

    //弹幕实际高宽
    public int mWidth = 0;
    public int mHeight = 0;
    public int mLineIndex = 0;
    public boolean mHasFollower = false;     //此行后面有无追随者弹幕
    public int mTarget = BarrageConfig.TYPE_HORIZONTAL;

    @Override
    public String toString() {
        return String.format("[%s,%f,%f]", mText, mDuration, mCurrentTime);
    }

    public AbsTrace() {
        mHolds[TraceFrame.ALPHA][0] = 1.0f;
        mHolds[TraceFrame.ALPHA][1] = 1.0f;
        mHolds[TraceFrame.SCALE_X][0] = 1.0f;
        mHolds[TraceFrame.SCALE_X][1] = 1.0f;
        mHolds[TraceFrame.SCALE_Y][0] = 1.0f;
        mHolds[TraceFrame.SCALE_Y][1] = 1.0f;
    }

    public boolean isUseBitmap() {
        return mUseBitmap;
    }

    public TraceFrame getCurrentFrame() {
        return mCurrentFrame;
    }

    public float getDuration() {
        return mDuration;
    }

    public float getCurrentTime() {
        return mCurrentTime;
    }

    public abstract AbsTrace x(float start, float end);

    public abstract AbsTrace y(float start, float end);

    public AbsTrace alpha(float start, float end) {
        mHolds[TraceFrame.ALPHA][0] = start;
        mHolds[TraceFrame.ALPHA][1] = end;
        return this;
    }

    public AbsTrace scaleX(float start, float end) {
        mHolds[TraceFrame.SCALE_X][0] = start;
        mHolds[TraceFrame.SCALE_X][1] = end;
        return this;
    }

    public AbsTrace scaleY(float start, float end) {
        float[] scaleY = {start, end};
        mHolds[TraceFrame.SCALE_Y][0] = start;
        mHolds[TraceFrame.SCALE_Y][1] = end;
        return this;
    }

    public AbsTrace duration(float duration) {
        mDuration = duration;
        return this;
    }

    public AbsTrace beginTime(float beginTime) {
        mBeginTime = beginTime;
        return this;
    }

    public AbsTrace setListener(OnAnimationListener listener) {
        mListener = listener;
        if (listener instanceof AnimationListenerImpl) {
            AnimationListenerImpl impl = (AnimationListenerImpl) listener;
            impl.setTarget(this);
        }
        return this;
    }

    public AbsTrace setRepeatCount(int count) {
        mRepeatCount = count;
        return this;
    }

    public AbsTrace setRepeatModel(int repeatModel) {
        mRepeatModel = repeatModel;
        return this;
    }

    public void start(IRenderConfig holder) {
        init();
        holder.addAnimation(this);
    }

    public void init() {
        float[] x = mHolds[TraceFrame.X];
        float[] y = mHolds[TraceFrame.Y];
        float[] alpha = mHolds[TraceFrame.ALPHA];
        float[] scaleX = mHolds[TraceFrame.SCALE_X];
        float[] scaleY = mHolds[TraceFrame.SCALE_Y];

        mSpeeds[TraceFrame.X] = (x[END] - x[START]) / mDuration;
        mSpeeds[TraceFrame.Y] = (y[END] - y[START]) / mDuration;
        mSpeeds[TraceFrame.ALPHA] = (alpha[END] - alpha[START]) / mDuration;
        mSpeeds[TraceFrame.SCALE_X] = (scaleX[END] - scaleX[START]) / mDuration;
        mSpeeds[TraceFrame.SCALE_Y] = (scaleY[END] - scaleY[START]) / mDuration;

        mCurrentFrame.mX = x[START];
        mCurrentFrame.mY = y[START];
        mCurrentFrame.mAlpha = alpha[START];
        mCurrentFrame.mScaleX = scaleX[START];
        mCurrentFrame.mScaleY = scaleY[START];

        mCurrentTime = 0.0f;
    }

    public void stepCurrentFrame(int key, float stepSize) {
        switch (key) {
            case TraceFrame.X:
                mCurrentFrame.mX += stepSize;
                break;
            case TraceFrame.Y:
                mCurrentFrame.mY += stepSize;
                break;
            case TraceFrame.ALPHA:
                mCurrentFrame.mAlpha += stepSize;
                break;
            case TraceFrame.SCALE_X:
                mCurrentFrame.mScaleX += stepSize;
                break;
            case TraceFrame.SCALE_Y:
                mCurrentFrame.mScaleY += stepSize;
                break;
            default:
                break;
        }
    }

//    public void setCurrentFrame(int key, float sum) {
//        switch (key) {
//            case TraceFrame.X:
//                mCurrentFrame.mX = mHolds[TraceFrame.X][0] + sum;
//                break;
//            case TraceFrame.Y:
//                mCurrentFrame.mY = mHolds[TraceFrame.Y][0] + sum;
//                break;
//            case TraceFrame.ALPHA:
//                mCurrentFrame.mAlpha = mHolds[TraceFrame.ALPHA][0] + sum;
//                break;
//            case TraceFrame.SCALE_X:
//                mCurrentFrame.mScaleX = mHolds[TraceFrame.SCALE_X][0] + sum;
//                break;
//            case TraceFrame.SCALE_Y:
//                mCurrentFrame.mScaleY = mHolds[TraceFrame.SCALE_Y][0] + sum;
//                break;
//            default:
//                break;
//        }
//    }

    public void setAlpha(float alpha) {
        mCurrentFrame.mAlpha = alpha;
    }

    protected void exploreBullet(BulletBuilder.Bullet bullet) {
        mUid = bullet.getUid();
        mNickName = bullet.getNickName();
        mText = bullet.getText();
        mExplosive = bullet.mExplosive;
        mWidth = bullet.getPixelsWidth();
        mHeight = bullet.getPixelsHeight();
        mCurrentTimeMillis = System.currentTimeMillis();
        mHasFollower = false;
    }


    public abstract void recycle();

    public int getTarget() {
        return mTarget;
    }
}
