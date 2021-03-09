package tv.athena.live.barrage.render.area;


import android.util.SparseIntArray;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.queue.GunPowderQueue;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.trace.AbsTrace;

import java.util.ArrayList;
import java.util.List;

/**
 * 弹幕渲染区域基础类
 *
 * @author donghao
 */
public abstract class AbsBarrageArea {
    protected static final int InValid = -1948;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;
    protected int mRange;

    protected IRenderConfig mBarrageHolder;
    protected GunPowderQueue mGunPowderQueue;
    protected AbsTrace mBarrageCache = null;

    protected List<Boolean> mLockers = null;                                //用于锁住每一个行
    protected List<AnimationListenerImpl> mAnimationListenerImpl = null;    //每一行只有一个监听器，当这一行数据跑完的时候，会置locker为false

    protected final float mLineScale = 0.8f;

    //自动增长，华丽模式
    protected boolean mAutoIncrease;

    protected AbsBarrageArea(IRenderConfig barrage, int lineCount) {
        mBarrageHolder = barrage;
        mLockers = new ArrayList<>(lineCount);
        mAnimationListenerImpl = new ArrayList<>(lineCount);

        for (int i = 0; i < lineCount; ++i) {
            mLockers.add(false);
            mAnimationListenerImpl.add(createAnimationListener());
        }

        mGunPowderQueue = new GunPowderQueue();

        mLeft = 0;
        mTop = 0;
        mRight = 0;
        mBottom = 0;
    }

    public void setRect(int left, int top, int right, int bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;

        mRange = Math.abs(left - right);
    }

    public void offer(GunPowder gunPowder) {
        mGunPowderQueue.offer(gunPowder);
    }

    public void reset(boolean clearCache) {
        if (null != mBarrageCache) {
            mBarrageCache.recycle();
            mBarrageCache = null;
        }

        if (clearCache) {
            mGunPowderQueue.clear();
        }
        resetLockers();
    }

    public int getLineCount() {
        return mLockers.size();
    }

    public void setQueueLimited(int lineCount) {
        if (mBarrageHolder.isFixedQueue()) {
            mGunPowderQueue.setLimited(mBarrageHolder.getFixedLine());
        } else {
            mGunPowderQueue.setLimited(lineCount);
        }
    }

    public void setLineCount(int lineCount, List<AbsTrace> current) {
        BarrageLog.info("barrage", "setLineCount : %d", lineCount);
        if (lineCount == mLockers.size()) {
            return;
        }

        int size = mLockers.size();
        int diff = Math.abs(lineCount - size);
        if (lineCount < size) {
            for (int i = 1; i <= diff; ++i) {
                mLockers.remove(size - i);
                AnimationListenerImpl listener = mAnimationListenerImpl.get(size - i);
                if (null != listener) {
                    listener.setTarget(null);
                }
                mAnimationListenerImpl.remove(size - i);
            }
        } else {
            SparseIntArray temp = new SparseIntArray();
            for (AbsTrace an : current) {
                temp.put(an.mLineIndex, 0);
            }
            for (int i = 0; i < diff; ++i) {
                mLockers.add(size + i, temp.get(size + i, -1) != -1);
                mAnimationListenerImpl.add(size + i, createAnimationListener());
            }
        }
    }

    protected OnAnimationListener getAnimationListener(int index) {
        if (index < mAnimationListenerImpl.size()) {
            return mAnimationListenerImpl.get(index);
        } else {
            return null;
        }
    }

    private void resetLockers() {
        for (int i = 0; i < mLockers.size(); ++i) {
            mLockers.set(i, false);
        }
    }

    public void start(AbsTrace animation, IRenderConfig holder, int lineIndex) {
        switch (animation.getTarget()) {
            case BarrageConfig.TYPE_VERTICAL:
                float scale1 = mBarrageHolder.getScale();
                animation.y(mRange, -animation.mHeight * scale1);
                animation.scaleX(scale1, scale1);
                animation.scaleY(scale1, scale1);
                break;
            default:
                break;
        }

        if (lineIndex >= mLockers.size()) {
            BarrageLog.error("barrage", "lineIndex %d >= mLockers.size() %d, return", lineIndex, mLockers.size());
            return;
        }

        animation.mLineIndex = lineIndex;
        animation.setListener(getAnimationListener(lineIndex));

        animation.start(holder);
        mLockers.set(animation.mLineIndex, true);
    }

    //用于横屏
    public void start(AbsTrace animation, IRenderConfig holder, int lineIndex, int diff) {
        float scale = mBarrageHolder.getScale();
        //当有随机速度的时候，关闭追上逻辑
        if (diff > 0 && mGunPowderQueue.size() < BarrageConfig.OPEN_RANDOM_SPEED_SIZE) {
            animation.x(mRange + diff * scale, -animation.mWidth * scale);
        } else {
            animation.x(mRange, -animation.mWidth * scale);
        }
        animation.scaleX(scale, scale);
        animation.scaleY(scale, scale);
        start(animation, holder, lineIndex);
    }


    public void setAutoIncrease(boolean auto) {
        mAutoIncrease = auto;
    }

    public boolean isAutoIncrease() {
        return mAutoIncrease;
    }

    //(height - ShellBuilder.getDefaultBarrageHeight())/2; 这个是为了让不同高度的弹幕在同一行能够水平
    protected float getAnimationPosY(int height, int lineIndex) {
        return (BulletBuilder.getDefaultBarrageHeight() * mLineScale * mBarrageHolder.getScale() + mBarrageHolder.getLineSpace()) * lineIndex
                - mBarrageHolder.getLineSpace() + mTop - (height - BulletBuilder.getDefaultBarrageHeight()) / 2;
    }


    public abstract boolean calculateBarrageReal(AbsTrace an);

    protected abstract AnimationListenerImpl createAnimationListener();

    protected abstract AbsTrace fire(BulletBuilder.Bullet bullet, float x, float y);

    protected abstract AbsTrace createBulletTrace(BulletBuilder.Bullet bullet, int target);

    protected float toWorldPositionX(float positionX) {
        return positionX;
    }

    protected float toWorldPositionY(float positionY) {
        return positionY;
    }

    public void onCalculateFinish() {
//        BarrageLog.info("onCalculateFinish");
    }

}
