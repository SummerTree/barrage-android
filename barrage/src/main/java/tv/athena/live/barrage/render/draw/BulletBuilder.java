package tv.athena.live.barrage.render.draw;

import android.graphics.Paint;
import android.text.TextPaint;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.config.BarrageContext;
import tv.athena.live.barrage.config.BarrageLog;
import tv.athena.live.barrage.newcache.AbsDrawingCache;
import tv.athena.live.barrage.newcache.BarrageBitmapManager;
import tv.athena.live.barrage.newcache.DrawingFactory;

/**
 * 火药加工生产成弹夹
 */
public class BulletBuilder<CONTENT> {

    private TextPaint mTextPaint = new TextPaint();
    private int mCharWidth = 0;
    private static int mCharHeight = 0;
    private int[] mCharWidthEnglish = new int[127];
    private final DrawingFactory<CONTENT> mFireworkFactory;


    public BulletBuilder(int shellSize, int shadowRadius, DrawingFactory.BuildMachine<CONTENT> machine) {
        initPainter(shellSize, shadowRadius);
        mFireworkFactory = new DrawingFactory<>(mTextPaint, mCharWidth, mCharHeight, machine);
    }


    public BarrageBitmapManager getBitmapManager() {
        return mFireworkFactory.getBitmapManager();
    }

    private void initPainter(int shellSize, int shadowRadius) {
        mTextPaint.setTextSize(shellSize);
        mTextPaint.setColor(BarrageConfig.DefaultColor);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        if (BarrageConfig.sEnbaleStroke) {
            mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mTextPaint.setStrokeWidth(1f);
        }
        if (BarrageConfig.sEnableBold) {
            mTextPaint.setFakeBoldText(true);
        }
        if (BarrageConfig.sEnableShadow) {
            mTextPaint.setShadowLayer(shadowRadius, BarrageConfig.ShadowDX, BarrageConfig.ShadowDY, BarrageConfig.ShadowColor);
        }
        mTextPaint.setAntiAlias(true);

        mCharWidth = (int) (mTextPaint.measureText(BarrageContext.gContext.getString(BarrageConfig.Chinese)));
        if (0 >= mCharWidth) {
            BarrageLog.error("barrage_shell_builder", "char width %d error,set to %d ", mCharWidth, shellSize);
            mCharWidth = shellSize;
        }
        int temp;
        for (int i = 0; i <= '~'; i++) {
            temp = (int) (mTextPaint.measureText(String.valueOf((char) i)));
            mCharWidthEnglish[i] = 0 < temp ? temp : (int) (shellSize * 0.875f);
        }

        initBarrageHeight(mTextPaint);
    }

    /**
     * 获取默认的弹幕高度
     */
    public static int getDefaultBarrageHeight() {

        if (mCharHeight > 0) {
            return mCharHeight;
        }

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(BarrageConfig.sBaseLandscapeSize);

        initBarrageHeight(paint);

        return mCharHeight;
    }

    private static void initBarrageHeight(Paint paint) {
        float baseline = -paint.ascent() + 0.5f; // ascent() is negative
        mCharHeight = (int) (baseline + paint.descent() + 0.5f + BarrageConfig.LANDSCAPE_LINE_SPACE_INNER);
//        mCharHeight = (int) (baseline + paint.descent());
        if (0 >= mCharHeight) {
            int heigh = (int) (BarrageConfig.sBaseLandscapeSize * 1.1f + 1.0f);
            BarrageLog.error("barrage_shell_builder", "char height %d error,set to %d ", mCharHeight, heigh);
            mCharHeight = heigh;
        }
        BarrageLog.info("barrage_shell_builder", "mCharHeight = %d", mCharHeight);
    }

    public Bullet<CONTENT> gunPowderToBullet(GunPowder ammo) {
        Bullet<CONTENT> bullet = new Bullet<>(ammo.mUid, ammo.mPowder, ammo.mNickName, ammo.mExplosive, ammo.mIsOwnBarrage);
        bullet.mDuration = ammo.mDuration;
        bullet.mBeginTime = ammo.mBeginTime;

        //已经是一个包含bitmap的元素
        if (ammo.mCacheObject != null) {
            bullet.setCacheObject(ammo.mCacheObject);
            return bullet;
        }

        final AbsDrawingCache<CONTENT> cacheObject = mFireworkFactory.draw(ammo);
        if (cacheObject == null) {
            return null;
        }
        cacheObject.increaseReferenceCount();
        bullet.setCacheObject(cacheObject);
        return bullet;
    }

    public int[] getCharSize() {
        return new int[]{mCharWidth, mCharHeight};
    }

    public static class Bullet<CONTENT> {
        private AbsDrawingCache<CONTENT> mCachePixels;
        private float mDuration;
        private float mBeginTime;

        private long mUid = 0;
        private String mText;
        private String mNickName;
        public int mExplosive;
        public boolean mIsOwn = false;

        public Bullet(long uid, String text, String nickName, int explosive, boolean isOwn) {
            mUid = uid;
            mText = text;
            mNickName = nickName;
            mExplosive = explosive;
            mIsOwn = isOwn;
        }

        public AbsDrawingCache<CONTENT> getCacheObject() {
            return mCachePixels;
        }

        public int getPixelsWidth() {
            return mCachePixels.getWidth();
        }

        public int getPixelsHeight() {
            return mCachePixels.getHeight();
        }

        public long getUid() {
            return mUid;
        }

        public String getNickName() {
            return mNickName;
        }

        public String getText() {
            return mText;
        }

        public boolean hasPixels() {
            boolean condition1 = null != mCachePixels;
            if (condition1) {
                return true;
            } else {
                BarrageLog.error(BarrageConfig.TAG, "mCachePixels is null");
            }

            return false;
        }

        public void setCacheObject(AbsDrawingCache cacheObject) {
            if (cacheObject != null && cacheObject.getContent() != null) {
                mCachePixels = cacheObject;
            }
        }

        public float getBeginTime() {
            return mBeginTime;
        }

        public float getDuration() {
            return mDuration;
        }
    }
}
