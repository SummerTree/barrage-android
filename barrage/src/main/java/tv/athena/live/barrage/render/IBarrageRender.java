package tv.athena.live.barrage.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import tv.athena.live.barrage.GunPowder;

/**
 * @author donghao on 2018/9/6.
 */

public interface IBarrageRender {

    void resetSmooth();

    boolean isBarrageRenderOn();

    void setBarrageRenderOn(boolean isOpen);

    void ceaseFire(boolean clearAll);

    void offer(GunPowder gunPowder, int type);

    void setBarrageAlpha(float alpha);

    void onBarrageSizeChanged(int size);

    void setAutoIncrease(int type, boolean b);

    void setBarrageType(int type);

    boolean isBarrageOn();

    void setOrientation(int orientation, boolean fromFloating);

    void onRequireMarqueeInSurface(Bitmap bitmap, float widthPixels, long duration);

    void notifyDispSizeChanged(int width, int height);

    void start();

    void stop();

    void draw(Canvas canvas);

    boolean isStop();

    void queueEvent(Runnable task);

    long getCurrentTime();

//    float getCacheHitRate();

    void clearCanvas();
}
