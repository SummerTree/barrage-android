package tv.athena.live.barrage;

import android.graphics.Bitmap;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.newcache.AbsDrawingCache;

/**
 * 弹幕火药
 *
 * @author donghao
 */
public class GunPowder {

    /**
     * 弹幕优先级，优先级越高，显示也越在上
     **/
    public static final int EXPLOSIVE_NORMAL = 1;
    public static final int EXPLOSIVE_HIGH = 2;
    public static final int EXPLOSIVE_HIGH_SHOW = 3;

    public static final int EXPLOSIVE_SINGLE = 4;

    /**
     * 文字方向，从左到右，从上到下
     **/
    public static final int DIRECTION_LTR = 0;
    public static final int DIRECTION_TTB = 1;

    /**
     * 是否自己弹幕
     */
    public boolean mIsOwnBarrage = false;

    //基本弹幕信息
    /**
     * 优先级 {@link #EXPLOSIVE_NORMAL}
     **/
    public int mExplosive;
    /**
     * AbsDrawingCache
     * 弹幕内容
     **/
    public String mPowder;
    /**
     * 颜色
     **/
    public int mColor;
    /**
     * 弹幕显示时间
     **/
    public float mDuration;

    /**
     * 弹幕开始事件
     */
    public float mBeginTime;

    /**
     * 文字渲染方向 {@link #DIRECTION_LTR}
     **/
    public int mDirection = DIRECTION_LTR;


    //额外信息
    /****主要用于神镜头，清理标志，为true时，清理队列到ShellQueue.KCLean数量***/
    public boolean mNeedClean;

    /**
     * 用户id，主要用于弹幕举报
     **/

    public long mUid = 0;
    /**
     * 用户昵称，用于弹幕举报
     **/
    public String mNickName;

    /**
     * 已经自携带Bitmap和ByteBuffer，无需再draw
     **/
    public AbsDrawingCache mCacheObject;

    public Bitmap mRawBitmap;

    /**
     * 携带特殊结构体信息，tv，礼物等等
     **/
    public Object mAttachObject;

    public GunPowder(String powder, int explosive, int color, int direction, float duration) {
        this(powder, explosive, color, direction, duration, 0);
    }

    public GunPowder(String powder, int explosive, int color, int direction, float duration, float beginTime) {
        mPowder = powder;
        mExplosive = explosive;
        mColor = color;
        mDirection = direction;
        mDuration = duration;
        mBeginTime = beginTime;
    }

    public GunPowder(long uid, String nickName, String powder) {
        this(powder, GunPowder.EXPLOSIVE_HIGH,
                BarrageConfig.DefaultColor,
                GunPowder.DIRECTION_LTR,
                BarrageConfig.DEFAULT_DURATION);
        mUid = uid;
        mNickName = nickName;
    }

    /**
     * 直播间普通发言
     **/
    public GunPowder(long uid, String nickName, String powder, int explosive, int color, int direction, float duration) {
        this(powder, explosive, color, direction, duration);
        mUid = uid;
        mNickName = nickName;
    }

    /**
     * 用于神镜头
     */
    public GunPowder(boolean needClean, String powder, int explosive, int color, float duration) {
        this(powder, explosive, color, DIRECTION_LTR, duration);
        mNeedClean = needClean;
    }

    /**
     * @param object    携带的自定义数据
     * @param explosive
     * @param duration
     */
    public GunPowder(Object object, int explosive, float duration) {
        mAttachObject = object;
        mExplosive = explosive;
        mDuration = duration;
    }

    /**
     * 一般用于自定义布局渲染
     */
    public GunPowder(GunPowder gunPowder, Bitmap bitmap, int explosive, float duration) {
        if (gunPowder != null) {
            mUid = gunPowder.mUid;
            mPowder = gunPowder.mPowder;
            mNickName = gunPowder.mNickName;
        }
        mRawBitmap = bitmap;
        mExplosive = explosive;
        mDuration = duration;
    }

    // FIXME: 2018/12/3 取hash值做特征有冲撞概率
    public int getCharacteristic() {
        int result = 1;
        result = 31 * result + mExplosive;
        result = 31 * result + (mPowder == null ? 0 : mPowder.hashCode());
        result = 31 * result + mColor;
        result = 31 * result + mDirection;
        return result;
    }
}
