package tv.athena.live.barrage.stencil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.utils.DependencyProperty;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author donghao on 2018/7/24.
 * <p>
 * 弹幕遮罩数据解析，画遮罩区域
 */

public class StencilManager {

    private static final String TAG = StencilManager.class.getSimpleName();
    private static final StencilManager ourInstance = new StencilManager();

    public static StencilManager getInstance() {
        return ourInstance;
    }

    public final static int DRAW_AREA_WIDTH = 540;
    public final static int DRAW_AREA_HEIGHT = 324;

    private final static int MSG_DRAW_STENCIL = 0;
    private final static int MSG_CLOSE_STENCIL = 1;
    private final static int MSG_NOT_RECEIVE_STENCIL_DELAY = 2;
    private final static int MSG_DRAW_RECT_STENCIL = 4;
    private final static int MSG_CLEAR_RECT_STENCIL = 5;

    /***因为底层渲染是每一次渲染都会上抛数据，导致严重卡顿，这里需要限制每秒钟获取遮罩数据次数 10次 **/
    public final static int MAX_STENCIL_DRAW_TIMES_PER_SECOND = 12;
    public final static int STENCIL_DRAW_INTENAL_TIME = 1000 / MAX_STENCIL_DRAW_TIMES_PER_SECOND;

    private long mLastStencilDrawTime = 0;

    /***两个队列实现生产消费***/
    private Queue<ByteBuffer> mEmptyBuffers = new ConcurrentLinkedQueue();
    private Queue<ByteBuffer> mFullBuffers = new ConcurrentLinkedQueue();

    private ByteBuffer mRectByteBuffer = null;

    private Object mBufferLock = new Object();

    /*** 是否有数据 **/
    private volatile DependencyProperty<Boolean> mHasData = new DependencyProperty<>(false);

    private byte[] mNewestData = null;
    private byte[] mSpecifiedData = null;

    private Handler mHandler;

    //原点坐标
    private int mOriginalPointX = 0;
    private int mOriginalPointY = 0;

    //视频区域大小
    private int mVideoWidth;
    private int mVideoHeight;

    //横竖屏宽高
    private int mScreenWidth;
    private int mScreenHeight;

    private StencilPainter mStencilPainter;


    private boolean mReceiveFlag = true;

    private StencilManager() {
        WindowManager wm = (WindowManager) BarrageContext.gContext.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }
        DisplayMetrics dm = new DisplayMetrics();
        if (wm.getDefaultDisplay() == null) {
            return;
        }
        wm.getDefaultDisplay().getMetrics(dm);

        mVideoWidth = mScreenWidth = dm.widthPixels > dm.heightPixels ? dm.widthPixels : dm.heightPixels;
        mVideoHeight = mScreenHeight = dm.widthPixels < dm.heightPixels ? dm.widthPixels : dm.heightPixels;

        mStencilPainter = new StencilPainter(DRAW_AREA_WIDTH, DRAW_AREA_HEIGHT);

    }

    public void reset() {
        BarrageLog.info(TAG, "reset data");
        ByteBuffer byteBuffer = mFullBuffers.poll();
        while (byteBuffer != null) {
            mEmptyBuffers.offer(byteBuffer);
            byteBuffer = mFullBuffers.poll();
        }
    }

    public void openReceiveStencil() {
        mReceiveFlag = true;

        if (mHandler != null) {
            mHandler.removeMessages(MSG_NOT_RECEIVE_STENCIL_DELAY);
        }
    }

    public void closeStencil() {
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_NOT_RECEIVE_STENCIL_DELAY, 1000);
        }
    }

    public void closeStenciDirectly() {
        mHasData.set(false);
    }


    /**
     * 设置弹幕遮罩数据，当为null时，说明没有数据了
     *
     * @param bytes
     */
    public void setData(byte[] bytes) {
        if (bytes == null && hasData()) {
            reset();
        }

        if (bytes != null) {
            mHasData.set(true);
            if (mHandler != null) {
                mHandler.removeMessages(MSG_CLOSE_STENCIL);
            }
        } else {
            if (mHandler != null && hasData()) {
                mHandler.sendEmptyMessageDelayed(MSG_CLOSE_STENCIL, 1000);
            }
        }

        mNewestData = bytes;
    }

    public void setData(byte[] sei, int xoff, int yoff, int videoWidth, int videoHeight) {
        if (!mReceiveFlag) {
            return;
        }

        setData(sei);

        //设置视频区域原点
        mOriginalPointX = xoff;
        mOriginalPointY = yoff;

        //设置视频大小
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    /**
     * 是否有弹幕遮罩数据
     *
     * @return
     */
    public boolean hasData() {
        return mHasData.get();
    }

    public DependencyProperty.Entity<Boolean> getDataFlagEntity() {
        return mHasData.getEntity();
    }

    public ByteBuffer getStencilData() {
        return mFullBuffers.poll();
    }

    public void recycleByteBuffer(ByteBuffer byteBuffer) {
        mEmptyBuffers.offer(byteBuffer);
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    /**
     * 弹幕来获取遮罩数据
     */
    public void activateStencilDraw() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastStencilDrawTime < STENCIL_DRAW_INTENAL_TIME) {
            return;
        }

        if (mNewestData != null) {
            mSpecifiedData = mNewestData;
            mNewestData = null;

            if (mHandler == null) {
                initHandler();
            }
            mHandler.sendEmptyMessage(MSG_DRAW_STENCIL);
            mLastStencilDrawTime = currentTime;
        }
    }

    private void initHandler() {
        mHandler = new Handler(BarrageConfig.newStartHandlerThread("BarrageStencil", Process.THREAD_PRIORITY_DEFAULT).getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {

                switch (message.what) {
                    case MSG_DRAW_STENCIL:
                        byte[] datas = null;

                        if (mSpecifiedData != null) {
                            datas = mSpecifiedData;
                            mSpecifiedData = null;
                        }

                        if (datas != null) {
                            parseData(datas);
                        }
                        break;
//                    case MSG_DRAW_RECT_STENCIL:
//                        if(hasRectStencil()) {
//                            drawRectStencil();
//                        }
//                        break;
//                    case MSG_CLEAR_RECT_STENCIL:
//                        if(mRectByteBuffer != null){
//                            mRectByteBuffer.clear();
//                        }
//                        break;
                    case MSG_CLOSE_STENCIL:
                        BarrageLog.debug(TAG, "closed stencil");
                        mHasData.set(false);
                        break;
                    case MSG_NOT_RECEIVE_STENCIL_DELAY:
                        mHasData.set(false);
                        mReceiveFlag = false;
                        break;
                }

                return false;
            }
        });

        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(DRAW_AREA_WIDTH * DRAW_AREA_HEIGHT * 4);
        mEmptyBuffers.offer(byteBuffer1);
        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(DRAW_AREA_WIDTH * DRAW_AREA_HEIGHT * 4);
        mEmptyBuffers.offer(byteBuffer2);
    }


    /**
     * 弹幕遮挡Rect区域 -- begin
     **/
    public synchronized void setRect(Rect rect) {
        mStencilPainter.setRect(rect);

        if (rect == null) {
            if (mRectByteBuffer != null) {
                mRectByteBuffer.clear();
            }
        } else {
            drawRectStencil();
        }
    }

    public boolean hasRectStencil() {
        return mStencilPainter.hasRectStencil();
    }

    private synchronized void drawRectStencil() {
        Bitmap bitmap = mStencilPainter.drawRectStencil();
        if (bitmap == null) {
            return;
        }

        if (mRectByteBuffer == null) {
            mRectByteBuffer = ByteBuffer.allocateDirect(DRAW_AREA_WIDTH * DRAW_AREA_HEIGHT * 4);
        }

        mRectByteBuffer.clear();
        bitmap.copyPixelsToBuffer(mRectByteBuffer);
        mRectByteBuffer.position(0);
    }

    public synchronized ByteBuffer getRectByteBuffer() {
        return mRectByteBuffer;
    }
    /**
     * 弹幕遮挡Rect区域 -- end
     **/


    /**
     * 解析视频遮罩区域数据
     *
     * @param data
     */
    private void parseData(byte[] data) {
        ByteBuffer byteBuffer = mEmptyBuffers.poll();
        if (byteBuffer == null) {
            return;
        }

        Bitmap bitmap = mStencilPainter.drawStencil(data);
        if (bitmap == null) {
            mEmptyBuffers.offer(byteBuffer);
            return;
        }

        byteBuffer.clear();
        bitmap.copyPixelsToBuffer(byteBuffer);
        byteBuffer.position(0);

        mFullBuffers.offer(byteBuffer);
    }

    public int getOriginalPointX() {
        return mOriginalPointX;
    }

    public int getOriginalPointY() {
        return mOriginalPointY;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }


}
