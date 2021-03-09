package tv.athena.live.barrage.stencil;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.logger.MTPApi;
import tv.athena.live.barrage.utils.pack.Unpack;

import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * 2018/7/24.
 */
class StencilPainter {

    private final static int BLUR_RADIUS = 25;

    private ArrayList<float[]> mCoors;

    private Canvas mCanvas;

    /**
     * 此处考虑用ArrayList, 是因为这样可以避免创建很多
     * 数组，导致过多GC影响性能
     */
    private final static String TAG = "StencilPainter";

    private int mInnerColor;
    private int mOuterColor;

    private Paint mInnerPaint;
    private BlurMaskFilter mNormalBlurMaskFilter = new BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.NORMAL);
    private BlurMaskFilter mSolidBlurMaskFilter = new BlurMaskFilter(BLUR_RADIUS, BlurMaskFilter.Blur.SOLID);

    private Paint mOuterPaint;
    private Paint mPathPaint;

    private float mOutWidth;
    private float mOutHeight;

    private Path mPath;

    private Bitmap mBitmap;

    /**
     * 临时用作镂空一块区域，目前有用于精彩时刻
     */
    private Rect mRectTemp = null;

    StencilPainter(int width, int height) {
        mOutWidth = width;
        mOutHeight = height;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        initPainter();
    }

    void initPainter() {
        mOuterColor = Color.parseColor("#00000000");
        mInnerColor = Color.parseColor("#FFFFFFFF");

        mInnerPaint = new Paint();
        mInnerPaint.setColor(mInnerColor);
        mInnerPaint.setStrokeWidth(1);
        mInnerPaint.setStyle(Paint.Style.FILL);
        //高斯模糊，做出羽化效果
        mInnerPaint.setMaskFilter(mSolidBlurMaskFilter);

        //画红线
//        mPathPaint = new Paint();
//        mPathPaint.setColor(0xB0FF0000);
//        mPathPaint.setStrokeWidth(80);
//        mPathPaint.setStyle(Paint.Style.STROKE);
//        mPathPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
        //区域底色
//        mOuterPaint = new Paint();
//        mOuterPaint.setColor(mOuterColor);
//        mOuterPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();
        mCanvas = new Canvas();
        mCanvas.setBitmap(mBitmap);
    }

    synchronized void setRect(Rect rect) {
        mRectTemp = rect;
    }

    synchronized boolean hasRectStencil() {
        return mRectTemp != null;
    }

    Bitmap drawRectStencil() {
        if (mRectTemp != null) {
            synchronized (this) {
                if (mRectTemp != null) {
                    mBitmap.eraseColor(0x00000000);
                    mCanvas.drawRect(mRectTemp, mInnerPaint);
                    return mBitmap;
                }
            }
        }
        return null;
    }


    Bitmap drawStencil(byte[] datas) {
        if (BarrageConfig.isTestEnv()) {
            BarrageLog.debug(TAG, "seiData: " + Arrays.toString(datas));
        }

        try {
            mCoors = realParseSeiData(datas);
        } catch (BufferUnderflowException e) {
            BarrageLog.error(TAG, "realParseSeiData failed:", e);
            BarrageLog.error(TAG, "datas %s:", Arrays.toString(datas));
            MTPApi.DEBUGGER.crashIfDebug(TAG, "realParseSeiData");
        }

        if (mCoors == null || mCoors.size() <= 0) {
            if (BarrageConfig.isTestEnv()) {
                BarrageLog.error(TAG, "realParseSeiData result is null!");
            }
            return null;
        }

        mBitmap.eraseColor(0x00000000);

        mPath.reset();
        for (int i = 0; i < mCoors.size(); i++) {
            float[] coors = mCoors.get(i);
            if (coors == null || coors.length < 2) {
                //至少要两个点（X、Y)
                BarrageLog.error(TAG, "coors == null or length < 2!");
                continue;
            }
            mPath.moveTo(coors[0] * mOutWidth, coors[1] * mOutHeight);
            int j = 2;
            for (; j < coors.length - 1; j += 2) {
                mPath.lineTo(coors[j] * mOutWidth, coors[j + 1] * mOutHeight);
            }
            mPath.lineTo(coors[0] * mOutWidth, coors[1] * mOutHeight);
        }

        //mCanvas.drawRect(0, 0, mOutWidth, mOutHeight, mOuterPaint);

        /**这里之所以画三次SOLid方式，就是为了让羽化效果更加明显，不然太淡不宜看出来**/
        mInnerPaint.setMaskFilter(mNormalBlurMaskFilter);
        mCanvas.drawPath(mPath, mInnerPaint);
        mCanvas.drawPath(mPath, mInnerPaint);
        mInnerPaint.setMaskFilter(mSolidBlurMaskFilter);
        mCanvas.drawPath(mPath, mInnerPaint);

        return mBitmap;
    }

    private ArrayList<float[]> realParseSeiData(byte[] datas) {
        Unpack unpack = new Unpack(datas);
        //4字节，sei数据组数
        int seiCount = unpack.popInt();
        ArrayList<float[]> validSeiList = new ArrayList<>();
        //主播端放大了100万，下面的缩放系数应该相应除以anchorScale
        float anchorScale = 1000000;
        for (int i = 0; i < seiCount; i++) {
            //4字节，数据的字节数
            int seiDataCount = unpack.popInt();

            if (seiDataCount <= 33) {
                if (i + 1 < seiCount) {
                    //i+1 < groupNum，说明还有下一组数据
                    for (int k = 0; k < seiDataCount; k++) {
                        unpack.popUint8(); //跳过seiDataCount个字节
                    }
                    continue;
                }
                BarrageLog.error(TAG, "error seiDataCount = %d", seiDataCount);
                return null;
            }
            // 'HUYA:%08d:%016d:'%s
            //  HUYA:类型:长度:数据
            //4字节，"HUYA"
            char[] prefixs = new char[4];
            prefixs[0] = ((char) unpack.popUint8().toInt());
            prefixs[1] = ((char) unpack.popUint8().toInt());
            prefixs[2] = ((char) unpack.popUint8().toInt());
            prefixs[3] = ((char) unpack.popUint8().toInt());
            String prefix = new String(prefixs);
            if (!"HUYA".equals(prefix)) {
                BarrageLog.error(TAG, "prefix is not HUYA: %s", prefix);
                return null;
            }
            //1字节，冒号
            unpack.popUint8().toInt();
            //8字节，类型,暂时不用
            unpack.popUint64();
            //1字节，冒号
            unpack.popUint8();
            unpack.popUint64();
            //上面8字节+下面8字节，共16字节，长度值
            unpack.popUint64();
            //1字节，冒号
            unpack.popUint8();
            //1字节，版本号
            int version = unpack.popUint8().toInt();
            if (version != 1) {
                //版本号不同不解析
                return null;
            }
            //1字节，数据是否有改变
            boolean hasChanged = unpack.popUint8().toInt() == 1 ? true : false;
            if (!hasChanged) {
                BarrageLog.debug(TAG, "has changed: %b", hasChanged);
                return null;
            }

            //2字节，主播画布宽度
            int originalWidth = unpack.popUint16().toInt();
            if (originalWidth <= 0) {
                BarrageLog.error(TAG, "parse data error originalWidth == %d", originalWidth);
                return null;
            }
            //2字节，主播画布高度
            int originalHeight = unpack.popUint16().toInt();
            if (originalHeight <= 0) {
                BarrageLog.error(TAG, "parse data error originalHeight == %d", originalHeight);
                return null;
            }
            //4字节，x坐标缩放系数
            float xFactor = unpack.popUint32().toInt() / anchorScale;
            //4字节，y坐标缩放系数
            float yFactor = unpack.popUint32().toInt() / anchorScale;
            //1字节，polyCount，这组sei数据中人像的个数
            int polyCount = unpack.popUint8().toInt();

            for (int k = 0; k < polyCount; k++) {
                //1字节，pointCount
                int pointCount = unpack.popUint8().toInt();
                float[] c = new float[pointCount * 2];
                for (int j = 0; j < pointCount * 2; j += 2) {
                    //x坐标
                    c[j] = unpack.popUint8().toInt() * xFactor / ((float) originalWidth);
                    //y坐标
                    c[j + 1] = unpack.popUint8().toInt() * yFactor / ((float) originalHeight);
                }
                validSeiList.add(c);
            }

            BarrageLog.debug(TAG, "seiDataCount: %s, xFactor: %f, yFactor: %f, polyCount: %d",
                    seiDataCount, xFactor, yFactor, polyCount);
        }
        return validSeiList;
    }
}
