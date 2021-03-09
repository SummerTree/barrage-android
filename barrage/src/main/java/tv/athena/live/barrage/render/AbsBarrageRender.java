package tv.athena.live.barrage.render;

import android.content.res.Configuration;
import android.graphics.Canvas;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.newcache.DrawingFactory;
import tv.athena.live.barrage.render.area.AbsBarrageArea;
import tv.athena.live.barrage.render.area.FlashArea;
import tv.athena.live.barrage.render.area.FloatingArea;
import tv.athena.live.barrage.render.area.HorizontalArea;
import tv.athena.live.barrage.render.area.VerticalArea;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.trace.AbsTrace;
import tv.athena.live.barrage.utils.pool.ArrayListPoolFactory;
import tv.athena.live.barrage.view.IBarrageView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 弹幕渲染基础抽象类
 *
 * @author donghao on 2018/8/28.
 */
abstract class AbsBarrageRender<T extends AbsTrace, DRAWING_TYPE> implements IRenderConfig<T>, IBarrageRender, DrawingFactory.BuildMachine<DRAWING_TYPE> {

    private static final String TAG = BarrageConfig.TAG_RENDER;

    BulletBuilder<DRAWING_TYPE> mBulletBuilder;

    private float mAlpha;
    private float mScale;
    private int mType = -1;

    private IBarrageView mBarrageView;

    private HorizontalArea mHorizontalRect;
    private VerticalArea mVerticalRect;
    private FlashArea mFlashRect;
    private FloatingArea mFloatingArea;

    private AtomicInteger mOrientation;

    //弹幕总开关，模式转换而来
    private AtomicBoolean mBarrageOn = new AtomicBoolean(false);
    //只是渲染开关，是否暂停渲染
    private AtomicBoolean mBarrageRenderOn = new AtomicBoolean(true);


    private ArrayList<T> mAnimations;
    private ArrayListPoolFactory mArrayListPoolFactory = new ArrayListPoolFactory(4);

    public AbsBarrageRender(IBarrageView iBarrageView, int type, boolean autoIncrease, int orientation, float alpha, int size) {
        super();
        mBarrageView = iBarrageView;
        mAnimations = mArrayListPoolFactory.obtain();

        mAlpha = alpha;
        mBulletBuilder = new BulletBuilder<>(BarrageConfig.sBaseLandscapeSize, BarrageConfig.ShadowRadius, this);

        mHorizontalRect = new HorizontalArea(this, orientation == Configuration.ORIENTATION_LANDSCAPE ?
                BarrageConfig.LANDSCAPE_LINE_COUNT : BarrageConfig.PORTRAIT_SIMPLIFY_LINE_COUNT) {
            @Override
            public AbsTrace createBulletTrace(BulletBuilder.Bullet bullet, int target) {
                return createTrace(bullet, target);
            }

            @Override
            protected float toWorldPositionX(float positionX) {
                return toCustomWorldPositionX(positionX);
            }
        };

        mVerticalRect = new VerticalArea(this, BarrageConfig.VerticalLineCount) {
            @Override
            public AbsTrace createBulletTrace(BulletBuilder.Bullet bullet, int target) {
                return createTrace(bullet, target);
            }

            @Override
            protected float toWorldPositionY(float positionY) {
                return toCustomWorldPositionY(positionY);
            }
        };

        mFlashRect = new FlashArea(this, BarrageConfig.FlashCount) {
            @Override
            public AbsTrace createBulletTrace(BulletBuilder.Bullet bullet, int target) {
                return createTrace(bullet, target);
            }
        };

        mFloatingArea = new FloatingArea(this, BarrageConfig.FLOATING_LINE_COUNT) {
            @Override
            protected AbsTrace createBulletTrace(BulletBuilder.Bullet bullet, int target) {
                return createTrace(bullet, target);
            }
        };

        mOrientation = new AtomicInteger(orientation);
        mHorizontalRect.setDuration(orientation);
        setLineCountByType(type);

        setAutoIncrease(type, autoIncrease);
        initScale(size);
    }

    @Override
    public boolean isBarrageRenderOn() {
        return mBarrageRenderOn.get();
    }

    @Override
    public void setBarrageRenderOn(boolean isRenderOn) {
        BarrageLog.info(TAG, "enter setBarrageRenderOn:%b", isRenderOn);
        mBarrageRenderOn.set(isRenderOn);
    }

    protected abstract AbsTrace createTrace(BulletBuilder.Bullet<DRAWING_TYPE> bullet, int target);

    protected float toCustomWorldPositionX(float positionX) {
        return positionX;
    }

    protected float toCustomWorldPositionY(float positionY) {
        return positionY;
    }

//    protected abstract AbsTrace createAnimation(
//            long uid, String nickName, String text, int explosive,
//            ShellCache.CacheObject texture, int textureWidth, int textureHeight,
//            int itemWith, int itemHeight, int target);

    protected void recycleUnusedFrame() {
        ArrayList<T> animations = pollAnimations();

        for (T an : animations) {
            if (an.mDuration <= an.mCurrentTime && !repeat(an)) {
                an.recycle();
                if (null != an.mListener) {
                    an.mListener.onAnimationEnd(an);
                }
            } else {
                addAnimation(an);
            }
        }

        pollAnimationsEnd(animations);
    }

    private boolean repeat(T an) {
        if (AbsTrace.INFINITE != an.mRepeatCount && an.mRepeatCount <= an.mRepeated) {
            return false;
        }

        if (an.mRepeatCount > an.mRepeated) {
            an.mRepeated += 1;
        }

        if (AbsTrace.REVERSE == an.mRepeatModel) {
            int size = an.mHolds.length;
            for (int i = 0; i < size; ++i) {
                float[] value = an.mHolds[i];
                float t = value[0];
                value[0] = value[1];
                value[1] = t;
            }
        }

        an.init();

        return true;
    }

    @Override
    public void setOrientation(int orientation, boolean isFromFloating) {
        BarrageLog.info(TAG, "mOrientation.get() = %d,  orientation = %d", mOrientation.get(), orientation);
        if (mOrientation.get() != orientation || isFromFloating) {
            mOrientation.set(orientation);
            //reset 弹幕字体大小
            initScale(BarrageConfig.DEFAULT_BARRAGE_SIZE);
        }
        mHorizontalRect.setDuration(orientation);
    }

    private void initScale(int size) {
        if (size == BarrageConfig.DEFAULT_BARRAGE_SIZE) {
            if (mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE) {
                size = BarrageConfig.getBarrageSize();
            } else {
                size = BarrageConfig.getVerticalBarrageSize();
            }
        }
        BarrageLog.info(TAG, "initBarrageSize = %d", size);
        mScale = 1.0f * size / BarrageConfig.sBaseLandscapeSize;
    }

    public void setRect(int left, int top, int right, int bottom) {
        mVerticalRect.setRect(right / 10, 0, right, bottom);
        mFlashRect.setRect(0, bottom / 2, right, bottom);

        if (!mBarrageView.hasCustomMargin()) {
            if (mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE) {
                top = BarrageConfig.LANDSCAPE_TOP_MARGIN;
                bottom = bottom - BarrageConfig.LANDSCAPE_BOTTOM_MARGIN;
            } else {
                top = BarrageConfig.PORTRAIT_TOP_MARGIN;
                bottom = bottom - BarrageConfig.PORTRAIT_BOTTOM_MARGIN;
            }
        } else {
            top = BarrageConfig.FLOATING_TOP_MARGIN;
        }
        mHorizontalRect.setRect(0, top, right, bottom);
        mFloatingArea.setRect(0, top, right, bottom);
        setAutoIncreaseLineCount(mHorizontalRect.isAutoIncrease());
    }

    /***
     * 設置横屏弹幕的弹幕层数
     * @param auto
     */
    private void setAutoIncreaseLineCount(boolean auto) {
        BarrageLog.debug(BarrageConfig.TAG, "setAutoIncreaseLineCount, %b", auto);
        if (auto) {
            int count = mHorizontalRect.resetMaxLineCount();
            BarrageLog.debug(BarrageConfig.TAG, "setAutoIncreaseLineCount, %b, count:%d", auto, count);
            mFloatingArea.resetMaxLineCount(count);
        } else {
            setLineCount(mHorizontalRect, BarrageConfig.TYPE_HORIZONTAL,
                    mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE ?
                            BarrageConfig.LANDSCAPE_LINE_COUNT : BarrageConfig.PORTRAIT_SIMPLIFY_LINE_COUNT);

            setLineCount(mFloatingArea, BarrageConfig.TYPE_FLOATING,
                    mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE ?
                            BarrageConfig.LANDSCAPE_LINE_COUNT : BarrageConfig.PORTRAIT_SIMPLIFY_LINE_COUNT);
        }
    }

    @Override
    public void offer(GunPowder gunPowder, int type) {
        switch (type) {
            case BarrageConfig.TYPE_VERTICAL:
                mVerticalRect.offer(gunPowder);
                break;
            case BarrageConfig.TYPE_FLASH:
                mFlashRect.offer(gunPowder);
                break;
            case BarrageConfig.TYPE_HORIZONTAL:
                mHorizontalRect.offer(gunPowder);
                break;
            case BarrageConfig.TYPE_FLOATING:
                mFloatingArea.offer(gunPowder);
                break;
            default:
                break;
        }
    }

    public void cleanQueue(boolean clearCache) {
        mHorizontalRect.reset(clearCache);
        mVerticalRect.reset(clearCache);
        mFlashRect.reset(clearCache);
        mFloatingArea.reset(clearCache);
    }

    @Override
    public void ceaseFire(boolean clearAll) {
        ceaseFire(clearAll, true);
    }

    public void ceaseFire(boolean clearAll, boolean clearCache) {
        BarrageLog.info(TAG, "clearAll:%b, clearCache:%b", clearAll, clearCache);
        if (clearAll) {
            clearAnimations(null);
        } else {
            clearAnimations(mBarrageMatcher);
        }
        cleanQueue(clearCache);
    }

    public void setAlpha(float alpha) {
        if (alpha != mAlpha) {
            mAlpha = alpha;

            ArrayList<T> animations = getAnimations();
            for (T item : animations) {
                if (BarrageConfig.TYPE_FLASH != item.getTarget()) {
                    item.setAlpha(alpha);
                }
            }
        }
    }

    @Override
    public void onBarrageSizeChanged(int size) {
        float scale = 1.0f * size / BarrageConfig.sBaseLandscapeSize;
        if (scale == mScale) {
            return;
        }

        mScale = scale;
        int count = mHorizontalRect.resetMaxLineCount();
        mFloatingArea.resetMaxLineCount(count);
    }

    public int getBarrageType() {
        return mType;
    }

    private void setLineCountByType(int type) {
        if (type == mType) {
            return;
        }

        mType = type;

        if (BarrageConfig.TYPE_HORIZONTAL != (BarrageConfig.TYPE_HORIZONTAL & mType)) {
            setLineCount(mHorizontalRect, BarrageConfig.TYPE_HORIZONTAL, 0);
        }

        if (BarrageConfig.TYPE_FLOATING != (BarrageConfig.TYPE_FLOATING & mType)) {
            setLineCount(mFloatingArea, BarrageConfig.TYPE_FLOATING, 0);
        }

        setLineCount(mVerticalRect, BarrageConfig.TYPE_VERTICAL, BarrageConfig.TYPE_VERTICAL == (BarrageConfig.TYPE_VERTICAL & mType) ? BarrageConfig.VerticalLineCount : 0);
        setLineCount(mFlashRect, BarrageConfig.TYPE_FLASH, BarrageConfig.TYPE_FLASH == (BarrageConfig.TYPE_FLASH & mType) ? BarrageConfig.FlashCount : 0);

    }

    /**
     * @param type
     * @param autoIncrease 自动增长意味着所有通道全打开
     */
    @Override
    public void setAutoIncrease(int type, boolean autoIncrease) {
        if (BarrageConfig.TYPE_HORIZONTAL == (BarrageConfig.TYPE_HORIZONTAL & type)) {
            mHorizontalRect.setAutoIncrease(autoIncrease);
            mFloatingArea.setAutoIncrease(autoIncrease);
            setAutoIncreaseLineCount(autoIncrease);
        }
    }

    @Override
    public void setBarrageType(int type) {
        setLineCountByType(type);
        BarrageLog.info(TAG, "mOrientation = %d, GLBarrage = %d", mOrientation.get(), getBarrageType());
        if (BarrageConfig.TYPE_NULL != getBarrageType()) {
            mBarrageOn.set(true);
            BarrageLog.info(TAG, "setBarrageType mBarrageOn.set(true)");
        } else {
            mBarrageOn.set(false);
            BarrageLog.info(TAG, "setBarrageType mBarrageOn.set(false)");
        }
    }

    @Override
    public boolean isBarrageOn() {
        return mBarrageOn.get();
    }

    private synchronized void setLineCount(AbsBarrageArea rect, int type, int lineCount) {
        rect.setQueueLimited(lineCount);

        int count = rect.getLineCount();
        if (count == lineCount) {
            return;
        }

        if (lineCount < count) {
            ArrayList<T> animations = pollAnimations();
            for (T an : animations) {
                if (an.mLineIndex >= lineCount && type == an.getTarget()) {
                    an.recycle();
                } else {
                    addAnimation(an);
                }
            }
            pollAnimationsEnd(animations);

        }

        rect.setLineCount(lineCount, (List<AbsTrace>) getAnimations());

    }

    @Override
    public BulletBuilder getShellBuilder() {
        return mBulletBuilder;
    }

    @Override
    public float getAlpha() {
        return mAlpha;
    }

    @Override
    public float getScale() {
        return mScale;
    }

    @Override
    public int getLineSpace() {
        if (mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE) {
            return BarrageConfig.LANDSCAPE_LINE_SPACE;
        } else {
            return BarrageConfig.PORTRAIT_LINE_SPACE;
        }
    }

    @Override
    public int getSpaceX() {
        if (mOrientation.get() == Configuration.ORIENTATION_LANDSCAPE) {
            return BarrageConfig.LANDSCAPE_SPACE_X;
        } else {
            return BarrageConfig.PORTRAIT_SPACE_X;
        }
    }

    public void calculateBarrage(float delta) {
        //检查是否有空通道
        boolean needTryAddNewBarrage = onPreCalculateBarrage();

        ArrayList<T> animations = pollAnimations();

        for (T an : animations) {
            calculateCurrentFrameReal(an, delta);
            if (needTryAddNewBarrage && an != null && !an.mHasFollower) {
                needTryAddNewBarrage = tryAddFollower(an);
            }
            addAnimation(an);
        }

        pollAnimationsEnd(animations);

        if (needTryAddNewBarrage) {
            onCalculateFinish();
        }
    }

    private boolean onPreCalculateBarrage() {
        boolean preHorizontal = mHorizontalRect.onPreCalculateBarrage();
        mFloatingArea.onPreCalculateBarrage();
        return preHorizontal;
    }

    private void onCalculateFinish() {
        mHorizontalRect.onCalculateFinish();
    }

    private boolean tryAddFollower(AbsTrace an) {
        switch (an.getTarget()) {
            case BarrageConfig.TYPE_HORIZONTAL:
                return mHorizontalRect.calculateBarrageReal(an);
            case BarrageConfig.TYPE_VERTICAL:
                return mVerticalRect.calculateBarrageReal(an);
            case BarrageConfig.TYPE_FLASH:
                return mFlashRect.calculateBarrageReal(an);
            case BarrageConfig.TYPE_FLOATING:
                return mFloatingArea.calculateBarrageReal(an);
            default:
                break;
        }
        return true;
    }

    private OnRemoveAnimMatcher mBarrageMatcher = new OnRemoveAnimMatcher() {
        @Override
        public boolean isMatch(AbsTrace anim) {
            return !anim.isUseBitmap();
        }
    };


    public boolean isEmpty() {
        return mAnimations.isEmpty();
    }


    protected ArrayList<T> pollAnimations() {
        ArrayList<T> animations = mAnimations;
        mAnimations = mArrayListPoolFactory.obtain();
        return animations;
    }

    public void pollAnimationsEnd(ArrayList list) {
        mArrayListPoolFactory.recycle(list);
    }

    @Override
    public void addAnimation(T an) {
        mAnimations.add(an);
    }

    public void clearAnimations(OnRemoveAnimMatcher matcher) {
        BarrageLog.debug(TAG, "clearAnimations");
        // 清空动画时，筛选出需要清空的，传入空值，则全部清空
        if (matcher != null) {
            ListIterator<T> iterator = mAnimations.listIterator();
            T animation;
            while (iterator.hasNext()) {
                animation = iterator.next();
                if (matcher.isMatch(animation)) {
                    animation.recycle();
                    iterator.remove();
                }
            }
        } else {

            for (T an : mAnimations) {
                an.recycle();
            }

            mAnimations.clear();
        }
    }

    protected void calculateCurrentFrameReal(T an, float delta) {
        int size = an.mHolds.length;
        for (int i = 0; i < size; ++i) {
            an.stepCurrentFrame(i, an.mSpeeds[i] * delta);
        }
        an.mCurrentTime += delta;
    }

    @Override
    public ArrayList<T> getAnimations() {
        return mAnimations;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void draw(Canvas canvas) {

    }

    public interface OnRemoveAnimMatcher {
        boolean isMatch(AbsTrace anim);
    }

//    @Override
//    public float getCacheHitRate() {
//        return mBulletBuilder.getFireworkFactory().getCacheHitRate();
//    }

    @Override
    public boolean isFixedQueue() {
        return mBarrageView.isQueueFixed();
    }

    @Override
    public int getFixedLine() {
        return mBarrageView.getQueueLine();
    }

    @Override
    public void clearCanvas() {

    }

    @Override
    public void sendMsg(Object object) {
        mBarrageView.sendMsg(object);
    }
}
