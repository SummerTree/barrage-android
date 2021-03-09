package tv.athena.live.barrage.render.area;

import android.content.res.Configuration;

import tv.athena.live.barrage.BarrageEvent;
import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.report.BarrageCacheForReport;
import tv.athena.live.barrage.stencil.StencilManager;
import tv.athena.live.barrage.trace.AbsTrace;

import java.util.Random;

/**
 * 左右（横方向）方向弹幕
 */
public abstract class HorizontalArea extends AbsBarrageArea {

    final private static String TAG = BarrageConfig.TAG;

    protected boolean mSingle = true;          //单数行通道显示，或者全部通道显示

    private int mMaxLineCount;

    Random random = new Random();

    private int mOrientationDuration = BarrageConfig.DEFAULT_DURATION;

    private Configuration mConfiguration = BarrageContext.gContext.getResources().getConfiguration();

    public HorizontalArea(IRenderConfig iRenderConfig, int lineCount) {
        super(iRenderConfig, lineCount);
        mMaxLineCount = lineCount;
    }

    public boolean isSingle() {
        return mSingle;
    }

    /**
     * 如果是单行，当大于等于71的时候，设置双行
     * 如果是双行，缓存为13时，回到单行
     */
    public void initSingleDouble() {
        int size = mGunPowderQueue.size();
        if (size < 0) {
            BarrageLog.error(TAG, "initSingleDouble size < 0");
            return;
        }

        if (mSingle) {
            if (size >= BarrageConfig.DOUBLE_WHEN_SHELL_CACHE_SIZES) {
                BarrageLog.info(TAG, "open double");
                mSingle = false;
            }
        } else {
            if (size <= BarrageConfig.SINGLE_WHEN_SHELL_CACHE_SIZES) {
                BarrageLog.info(TAG, "close double");
                mSingle = true;
            }
        }
    }

    public void setDuration(int orientation) {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mOrientationDuration = BarrageConfig.VERTICAL_DEFAULT_DURATION;
        } else {
            mOrientationDuration = BarrageConfig.DEFAULT_DURATION;
        }
    }

    public int resetMaxLineCount() {
        int height = (int) Math.ceil(mBarrageHolder.getShellBuilder().getCharSize()[1] * mBarrageHolder.getScale());
        int lineSpace = mBarrageHolder.getLineSpace();
        mMaxLineCount = (int) (((float) (Math.abs(mBottom - mTop) + lineSpace)) / ((height + lineSpace) * mLineScale));
        if ((mAutoIncrease || mMaxLineCount < getLineCount()) && 0 <= mMaxLineCount) {
            setQueueLimited(mMaxLineCount);
            setLineCount(mMaxLineCount, mBarrageHolder.getAnimations());
        }
        return mMaxLineCount;
    }

    public boolean onPreCalculateBarrage() {
        //确定启用单行还是全通道
        initSingleDouble();

        //检查队列是否有空的，如果前面有空通道
        for (int i = 0; i < mLockers.size() && i < BarrageConfig.EMPTY_STRATEGY_LINE_NUMBER; i++) {
            if (!mLockers.get(i)) {
//                BarrageLog.debug(TAG, "onPreCalculateBarrage add to line " + i);
                startNewBarrageAnimation(i, 0);
                return false;
            }

            if (isSingle()) {
                i++;
            }
        }

        return true;
    }

    @Override
    public boolean calculateBarrageReal(AbsTrace an) {
        int spaceX = mBarrageHolder.getSpaceX();

        if (isSingle() && an.mLineIndex % 2 == 1) {
            return true;
        }

        if (mRange - an.mWidth * an.getCurrentFrame().scaleX() - spaceX >
                toWorldPositionX(an.getCurrentFrame().x())) {

//            BarrageLog.debug(TAG, "mRange=%d, an.mWidth= %d, an.mWidth * an.getCurrentFrame().scaleX()=%f" +
//                            ", spaceX=%d, an.getCurrentFrame().x()=%f," +
//                            "toWorldPositionX(an.getCurrentFrame().x()) = %f ",
//                    mRange,an.mWidth, an.mWidth * an.getCurrentFrame().scaleX(), spaceX,
//                    an.getCurrentFrame().x(), toWorldPositionX(an.getCurrentFrame().x()));

            //检查队列是否有空的，如果前面有空通道
            int index = mLockers.size();
            for (int i = 0; i < mLockers.size(); ++i) {
                if (!mLockers.get(i)) {
                    index = i;
                    break;
                }
                if (isSingle()) {
                    i++;
                }
            }

            //index > an.mLineIndex 如果前面有空通道，就不用在此通道添加新弹幕
            if (mLockers.size() > an.mLineIndex && index > an.mLineIndex) {
//                BarrageLog.info("wolf", "index : %d", index);
                if (startNewBarrageAnimation(an.mLineIndex, an.mWidth)) {
                    an.mHasFollower = true;
                }
                return false;
            }
        }

        return true;
    }

    private long mLastTimt = 0;
    private static final int SHOW_ANTIBLOCK_TIPS_INTENAL = 4000;

    /**
     * 该方法里的判断顺序影响性能
     * 这部分耦合度比较高，但是为了性能
     */
    private void tryShowAntiBlockingBarrageTip() {
        //已经提示过了，就不再提示
        if (BarrageConfig.isAntiBlockHasTip()) {
            return;
        }

        //判断当前是否处于横屏状态下
        if (mConfiguration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        //判断当前直播品类是否在功能范围内
        if (!StencilManager.getInstance().hasData()) {
            return;
        }
        //判断用户是否主动设置过防遮挡弹幕
        if (!BarrageConfig.isAntiBlockNeverUse()) {
            return;
        }

        //判断当前弹幕是否为华丽弹幕模式
        int mode = BarrageConfig.getBarrageModel();
        if (BarrageConfig.ModelLuxury != mode) {
            return;
        }
        //条件达到,发送事件弹出引导
        if (System.currentTimeMillis() - mLastTimt > SHOW_ANTIBLOCK_TIPS_INTENAL) {
            mBarrageHolder.sendMsg(new BarrageEvent.ShowAntiBlockTip());
            mLastTimt = System.currentTimeMillis();
        }
    }

    /**
     * 在某个通道添加一个新弹幕
     *
     * @param lineIndex 通道index
     * @return 队列是否有弹幕
     */
    private boolean startNewBarrageAnimation(int lineIndex, int lastWidth) {

//        BarrageLog.debug(TAG, "startNewBarrageAnimation, index =  " + lineIndex);
        //弹幕占到6个及以上时触发，0 2 4 6 8 10
        if (lineIndex > 9) {
            tryShowAntiBlockingBarrageTip();
        }

        AbsTrace barrageAnimation = pollBarrage(lineIndex);
        if (null == barrageAnimation) {
            return false;
        }

        BarrageCacheForReport.getInstance().add(barrageAnimation);

        float yPos = getAnimationPosY(barrageAnimation.mHeight, lineIndex);
        float duration = adjustShowDuration(mOrientationDuration);
        //duration = duration * (mRange + barrageAnimation.mWidth * mBarrageHolder.getScale()) / mRange;

        barrageAnimation.y(yPos, yPos);
        //adjust speed
        barrageAnimation.duration(duration);
        start(barrageAnimation, mBarrageHolder, lineIndex, barrageAnimation.mWidth - lastWidth);
        return true;
    }

    private AbsTrace pollBarrage(int lineIndex) {
        AbsTrace result = null;
        GunPowder ammo = mGunPowderQueue.poll(lineIndex);
        while (ammo != null && !isGunPowderValid(ammo)) {
            //细微调整：从后面取的原因是，怕前面都是一些需要渲染的，导致卡顿，后面一般是普通弹幕，可以减轻压力
            ammo = mGunPowderQueue.poll(true);
        }

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

    /*** 礼物和表情需要抛出消息特殊处理 ***/
    private boolean isGunPowderValid(GunPowder ammo) {
        if (ammo.mCacheObject != null) {
            return true;
        }

        //携带附件的火药
        if (ammo.mAttachObject != null) {
            mBarrageHolder.sendMsg(ammo.mAttachObject);
            return false;
        }

        return true;
    }


    @Override
    public void onCalculateFinish() {
        for (int i = 0; i < mLockers.size(); ++i) {
            if (!mLockers.get(i)) {
                startNewBarrageAnimation(i, 0);
                return;
            }

            if (isSingle()) {
                i++;
            }

        }
    }


    private float adjustShowDuration(float duration) {

        //整体加速速度调整
        float result = mGunPowderQueue.adjustShowDuration(duration);

        //大于KRAMDOM_OPEN的才有随机速度
        if (mGunPowderQueue.size() < BarrageConfig.OPEN_RANDOM_SPEED_SIZE) {
            return result;
        }

        //三分之一的速度明显加快，另外三分之二慢的随机速度继续降低其影响范围到1/2
        int s = random.nextInt(100) % (36) + 65;
        if (s > 77) {
            s = (100 + s) / 2;
        }
        result = result * s / 100;

        //最低通过时间4.5
        if (result < 4.5f) {
            result = 4.5f;
        }

        return result;
    }

    @Override
    protected AbsTrace fire(BulletBuilder.Bullet bullet, float x, float y) {
        if (bullet.hasPixels()) {
            AbsTrace an = createBulletTrace(bullet, BarrageConfig.TYPE_HORIZONTAL);
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

    @Override
    protected AnimationListenerImpl createAnimationListener() {
        return new AnimationListenerImpl() {
            @Override
            protected void onLastItemEnd(AbsTrace last) {
                if (last.mLineIndex >= mLockers.size()) {
                    return;
                }
//                BarrageLog.debug(TAG, "onLastItemEnd index=" + last.mLineIndex);
                mLockers.set(last.mLineIndex, false);
            }
        };
    }

}
