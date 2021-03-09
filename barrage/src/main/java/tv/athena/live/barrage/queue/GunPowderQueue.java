package tv.athena.live.barrage.queue;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.logger.MTPApi;
import tv.athena.live.barrage.report.BarrageLost;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 火药缓存队列
 *
 * @author donghao
 */
public class GunPowderQueue {
    private static final String TAG = BarrageConfig.TAG_QUEUE;
    public static final int KLIMIT = 400;
    public static final int KREMOVE = 20;       //每次捨棄的彈幕數量

    public static final int KSINGLE_REMOVE = 10;
    public static final int KSINGLE_MAX = 20;

    public static final int KCACHE_NUMBER_PER_LINE = 20;       //每一行弹幕缓存最大数量

    private int mQueueMaxNumb = KLIMIT;

    private final static int MAX_LINES = 22;
    private final static int DISCARD_SIZE = 10;
    private int mDiscardNum = 0;                        //丢弃弹幕数量

    //用于神镜头，大于5个的时候弹幕需要清理
    public static final int KCLEAN = 5;

    private ReentrantLock mLock = new ReentrantLock();
    private Deque<GunPowder> mShells;
    //仅用于特殊必须要在单通道显示，理论上已经渲染出来的，不影响原丢弃逻辑
    private Deque<GunPowder> mSingleLineShells;
    private long mPollCount = 1;

    public GunPowderQueue() {
        mShells = new LinkedList<>();
        mSingleLineShells = new LinkedList<>();
    }

    public void setLimited(int maxLine) {
        if (maxLine > 0) {
            mDiscardNum = MAX_LINES / maxLine - 1;
        }
        BarrageLog.info(TAG, "maxLine: %d, mDiscardNum:%d", maxLine, mDiscardNum);

        mQueueMaxNumb = KCACHE_NUMBER_PER_LINE * maxLine;
        if (mQueueMaxNumb > KLIMIT) {
            mQueueMaxNumb = KLIMIT;
        }
        BarrageLog.info(TAG, "maxLine:%d, mQueueMaxNumb:%d ", maxLine, mQueueMaxNumb);
    }

    public void offer(GunPowder item) {
        if (mLock.tryLock()) {

            if (item.mExplosive == GunPowder.EXPLOSIVE_SINGLE) {
                offerSingle(item);
                mLock.unlock();
                return;
            }

            //当item携带clean标识，清理队列到KCLean数量
            if (item.mNeedClean) {
                while (mShells.size() > KCLEAN) {
                    mShells.pollLast();
                }
            }

            if (mShells.size() >= mQueueMaxNumb) {
                BarrageLost.recordLostBarrage();
                for (int i = 0, lost = 0; i < mShells.size() && lost < KREMOVE; i++) {
                    //优先抛弃旧的弹幕
                    GunPowder p = mShells.peekFirst();
                    if (GunPowder.EXPLOSIVE_HIGH > p.mExplosive) {
                        mShells.pollFirst();
                        lost++;
                    } else {
                        break;
                    }
                }

                //check size again
                if (mShells.size() >= mQueueMaxNumb) {
                    for (int i = 0; i < KREMOVE; ++i) {
                        mShells.pollFirst();
                    }
                }
            }

            if (GunPowder.EXPLOSIVE_HIGH <= item.mExplosive) {
                mShells.offerFirst(item);
            } else {
                mShells.offer(item);
            }

            mLock.unlock();
        } else {
            //提升高优先级弹幕显示到屏幕上的概率
            if (mLock.tryLock()) {
                if (item.mExplosive == GunPowder.EXPLOSIVE_SINGLE) {
                    offerSingle(item);
                    mLock.unlock();
                    return;
                }

                if (GunPowder.EXPLOSIVE_HIGH <= item.mExplosive) {
                    mShells.offerFirst(item);
                }
                mLock.unlock();
            }
        }
    }

    private void offerSingle(GunPowder item) {
        if (mSingleLineShells.size() > KSINGLE_MAX) {
            for (int i = 0; i < KSINGLE_REMOVE; ++i) {
                mSingleLineShells.pollLast();
            }
        }
        mSingleLineShells.offer(item);
    }

    public GunPowder poll(int lineIndex) {
        if (lineIndex % 2 == 0) {
            if (mLock.tryLock()) {
                GunPowder powder = mSingleLineShells.poll();
                mLock.unlock();
                if (powder != null) {
                    return powder;
                }
            } else {
                BarrageLog.debug(TAG, "tryLock failed, lineindex:%s", lineIndex);
            }
        }

        return poll();
    }

    public GunPowder poll() {
        return poll(false);
    }

    public GunPowder poll(boolean isPollLast) {
        if (mLock.tryLock()) {
            GunPowder powder = null;
            try {
                //每8次从队列最后获取弹幕用来显示，确保如果全屏礼物弹幕也会有发言弹幕存在
                if (mPollCount % 8 == 0 || isPollLast) {
                    powder = mShells.pollLast();
                } else {
                    powder = mShells.poll();
                }
            } catch (NoSuchElementException e) {
                int queueSize = mShells.size();
                BarrageLog.error("ShellQueue", "[poll] throws NoSuchElementException, mLock=%d, mGunPowderQueue.size=%d, sizeBeforeCrash=%d", mLock.getHoldCount(), mShells.size(), queueSize);
                MTPApi.DEBUGGER.crashIfDebug(e, "[poll] throws NoSuchElementException, mLock=%d, mGunPowderQueue.size=%d, sizeBeforeCrash=%d", mLock.getHoldCount(), mShells.size(), queueSize);
            }

            if (powder != null) {
                //取一条丢几条逻辑
                if (mDiscardNum > 0 && mShells.size() > DISCARD_SIZE) {
                    for (int i = 0; i < mDiscardNum; i++) {
                        mShells.pollLast();
                    }
                }

                mPollCount++;
//                BarrageLog.debug(TAG, "nickName: %s, text: %s, size = %d", powder.mNickName, powder.mPowder, mShells.size());
            }

            mLock.unlock();
            return powder;
        }


        return null;
    }

    /**
     * 整体加速调整
     */
    public float adjustShowDuration(float duration) {
        int diff = mShells.size() - BarrageConfig.ACTIVATE_ACC_SIZE;
        if (diff > 0) {
            if (diff > BarrageConfig.ACC_DENOMINATOR) {
                diff = BarrageConfig.ACC_DENOMINATOR;
            }
            return duration * (1.0f - (BarrageConfig.ACCLERATE * ((float) diff / (float) BarrageConfig.ACC_DENOMINATOR)));
        }
        return duration;
    }

    public void clear() {
        //非阻塞，所以尝试100次获取锁清理队列
        for (int i = 0; i < 100; i++) {
            if (mLock.tryLock()) {
                mShells.clear();
                mSingleLineShells.clear();
                mLock.unlock();
                BarrageLog.debug(TAG, "mGunPowderQueue.clear, get lock times : %d", i);
                return;
            }
        }
        BarrageLog.error(TAG, "clear error, has not got lock");
    }

    public int size() {
        if (mLock.tryLock()) {
            int size = mShells.size();
            mLock.unlock();
            return size;
        }

        return -1;
    }
}
