package tv.athena.live.barrage.report;

import android.text.TextUtils;

import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.trace.AbsTrace;
import tv.athena.live.barrage.utils.GLCoordinate;
import tv.athena.live.barrage.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * 弹幕举报缓存
 *
 * @author dh on 2017/10/31.
 */

public class BarrageCacheForReport {

    private static String TAG = "BarrageCacheForReport";
    private static BarrageCacheForReport Instance = new BarrageCacheForReport();

    private static final int CACHE_MAX_SIZE = 100;
    private static final int CLEAN_SIZE_EVERY_TIME = 50;

    private static final int CACHE_TIME_AFTER_DISAPEARED = 5;     //弹幕从频幕消失5秒内数据
    private static final float TOUCH_AREA_RANGE = DensityUtil.dip2px(BarrageContext.gContext, 22);   //手势误差区域可以调整
    private static final float TOUCH_AREA_ERROR_RANGE_X = DensityUtil.dip2px(BarrageContext.gContext, 20);    //60f是测试出来的误差

    public static final int INVALID = -1;

    private Deque<AbsTrace> mAnimtionList = new LinkedList<>();

    private boolean mEnable = true;

    public static BarrageCacheForReport getInstance() {
        return Instance;
    }

    public void switchReport(boolean enable) {
        mEnable = enable;
    }

    public BarrageCacheForReport() {
    }

    /**
     * 为了不影响性能，这里最好尽量少做动作
     */
    public synchronized void add(AbsTrace barrageAnimation) {
        if (!mEnable) {
            return;
        }
        if (mAnimtionList.size() > CACHE_MAX_SIZE) {
            for (int i = 0; i < CLEAN_SIZE_EVERY_TIME; i++) {
                mAnimtionList.pollFirst();
            }
        }
        mAnimtionList.offer(barrageAnimation);
    }

    /**
     * 举报缓存清空
     */
    public synchronized void clear() {
        mAnimtionList.clear();
    }

    /**
     * 过滤缓存队列，超过5秒的可以去掉了
     */
    private synchronized void filter() {
        long currentTime = System.currentTimeMillis();
        AbsTrace item = mAnimtionList.peekFirst();
        while (item != null) {
            long diff = currentTime - item.mCurrentTimeMillis;
            if (diff > 0 && (diff > (item.getDuration() + CACHE_TIME_AFTER_DISAPEARED * 1000))) {
                BarrageLog.debug(TAG, "remove:%s", item.mText);
                mAnimtionList.pollFirst();
                item = mAnimtionList.peekFirst();
            } else {
                break;
            }
        }
    }

    /**
     * 获取5秒内的弹幕缓存
     */
    public List<BarrageReportItem> getCachedBarrages() {
        return getBarrageByCoordinate(INVALID, INVALID);
    }

    /**
     * 根据坐标获取选中弹幕
     */
    public synchronized List<BarrageReportItem> getBarrageByCoordinate(float x, float y) {
        BarrageLog.info(TAG, "enter getBarrageByCoordinate, x = %f, y = %f, TOUCH_AREA_RANGE = %f", x, y, TOUCH_AREA_RANGE);
        List<BarrageReportItem> list = new ArrayList<>();

        filter();       //过滤5秒前缓存

        for (AbsTrace item : mAnimtionList) {
            if (matchCoordinate(item, x, y)) {
                BarrageReportItem barrageReportItem = new BarrageReportItem(item.mUid, item.mNickName, item.mText);
                list.add(barrageReportItem);
            }
        }

        for (BarrageReportItem item : list) {
            BarrageLog.debug(TAG, "touched barrage:  %s, nickName: %s", item.getUid(), item.getNickName());
        }
        return list;
    }

    /**
     * 判断缓存的弹幕是否满足条件
     */
    private boolean matchCoordinate(AbsTrace animation, float x, float y) {

        if (animation.mUid == 0 || TextUtils.isEmpty(animation.mText)) {
            return false;
        }

        if (x == INVALID && y == INVALID) {
            return true;
        }

        //弹幕已结束
        if (animation.mTextureId == -1) {
            return false;
        }

        float distance = getDistanceToBarrage(animation, x, y);
        if (distance <= TOUCH_AREA_RANGE) {
            return true;
        }

        return false;
    }

    /**
     * 获取点到矩形的最小垂直距离
     * 比较粗略的计算，仅满足需求即可
     */
    private float getDistanceToBarrage(AbsTrace animation, float x, float y) {
        float width = animation.mWidth;
        float height = animation.mHeight;
        float barrageX = GLCoordinate.toWorldPositionX(animation.getCurrentFrame().x()) + TOUCH_AREA_ERROR_RANGE_X;
        float barrageY = GLCoordinate.toWorldPositionY(animation.getCurrentFrame().y());
        BarrageLog.debug(TAG, "text:%s,width:%f, height:%f, barrageX:%f, barrageY:%f, x:%f, y:%f", animation.mText, width, height, barrageX, barrageY, x, y);

        if (x < barrageX) {
            //点在左边
            return getMaxDistance(barrageX - x, getMinDistance(barrageY - y, barrageY + height - y));
        } else if (x > barrageX + width) {
            //点在右边
            return getMaxDistance(x - barrageX - width, getMinDistance(barrageY - y, barrageY + height - y));
        } else {
            //点在中间
            return getMinDistance(barrageY - y, barrageY + height - y);
        }
    }


    private float getMinDistance(float a, float b) {
        float aAbs = Math.abs(a);
        float bAbs = Math.abs(b);
        return aAbs < bAbs ? aAbs : bAbs;
    }

    private float getMaxDistance(float a, float b) {
        float aAbs = Math.abs(a);
        float bAbs = Math.abs(b);
        return aAbs > bAbs ? aAbs : bAbs;
    }


    public static class BarrageReportItem {
        public long mUid;
        public String mNickName;
        public String mText;

        public BarrageReportItem(long uid, String nickName, String text) {
            mUid = uid;
            mNickName = nickName;
            mText = text;
        }

        public long getUid() {
            return mUid;
        }

        public String getContent() {
            return mText;
        }

        public String getNickName() {
            return mNickName;
        }
    }

}
