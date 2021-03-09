package tv.athena.live.barrage.render.area;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.report.BarrageCacheForReport;
import tv.athena.live.barrage.trace.AbsTrace;

import static tv.athena.live.barrage.config.BarrageConfig.TAG;

/**
 * 悬浮弹幕
 *
 * @author dh on 2019-09-06
 */
public abstract class FloatingArea extends AbsBarrageArea {

    private int mMaxLineCount;

    protected FloatingArea(IRenderConfig barrage, int lineCount) {
        super(barrage, lineCount);
        mMaxLineCount = lineCount;
    }

    @Override
    public boolean calculateBarrageReal(AbsTrace an) {
        return true;
    }

    @Override
    protected AnimationListenerImpl createAnimationListener() {
        return new AnimationListenerImpl() {
            @Override
            protected void onLastItemEnd(AbsTrace last) {
                if (last.mLineIndex >= mLockers.size()) {
                    return;
                }
                mLockers.set(last.mLineIndex, false);
            }
        };
    }

    public void resetMaxLineCount(int count) {
        if ((mAutoIncrease || count < getLineCount()) && 0 <= count) {
            setQueueLimited(count);
            setLineCount(count, mBarrageHolder.getAnimations());
        }
    }

    public boolean onPreCalculateBarrage() {
        //检查队列是否有空的，如果前面有空通道
        for (int i = 1; i < mLockers.size(); i += 2) {
            if (!mLockers.get(i)) {
                startNewBarrageAnimation(i);
                return false;
            }

        }

        return true;
    }

    @Override
    protected AbsTrace fire(BulletBuilder.Bullet bullet, float x, float y) {
        if (bullet.hasPixels()) {
            AbsTrace an = createBulletTrace(bullet, BarrageConfig.TYPE_FLOATING);
            //FIXME 很恶心的解决方法
            if (an == null) {
                //直接丢弃
                return null;
            }

            an.duration(bullet.getDuration());
            an.beginTime(bullet.getBeginTime());
            an.alpha(mBarrageHolder.getAlpha(), mBarrageHolder.getAlpha());

            if (InValid != y) {
                an.y(y, y);
            }

            return an;
        }

        return null;
    }

    /**
     * 在某个通道添加一个新弹幕
     *
     * @param lineIndex 通道index
     * @return 队列是否有弹幕
     */
    private void startNewBarrageAnimation(int lineIndex) {

        AbsTrace barrageAnimation = pollBarrage(lineIndex);
        if (null == barrageAnimation) {
            return;
        }

        BarrageCacheForReport.getInstance().add(barrageAnimation);

        float yPos = getAnimationPosY(barrageAnimation.mHeight, lineIndex);
        float xPos = (mRight - barrageAnimation.mWidth) / 2;
        float duration = BarrageConfig.DEFAULT_FLOATING_TIME_FURATION;
        //duration = duration * (mRange + barrageAnimation.mWidth * mBarrageHolder.getScale()) / mRange;

        barrageAnimation.y(yPos, yPos);
        barrageAnimation.x(xPos, xPos);
        //adjust speed
        barrageAnimation.duration(duration);
        start(barrageAnimation, mBarrageHolder, lineIndex);
    }


    private AbsTrace pollBarrage(int lineIndex) {
        AbsTrace result = null;
        GunPowder ammo = mGunPowderQueue.poll(lineIndex);
//        while (ammo != null) {
//            //细微调整：从后面取的原因是，怕前面都是一些需要渲染的，导致卡顿，后面一般是普通弹幕，可以减轻压力
//            ammo = mGunPowderQueue.poll(true);
//        }

        if (null != ammo) {
            BulletBuilder.Bullet bullet = mBarrageHolder.getShellBuilder().gunPowderToBullet(ammo);
            if (null != bullet) {
                result = fire(bullet, 0.0f, InValid);
            } else {
                BarrageLog.error(TAG, "gunPowderToBullet failed!");
            }
        }
        return result;
    }

}
