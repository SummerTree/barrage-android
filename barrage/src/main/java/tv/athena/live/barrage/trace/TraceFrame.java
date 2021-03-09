package tv.athena.live.barrage.trace;

import tv.athena.live.barrage.utils.GLCoordinate;

public class TraceFrame {
    public static final int X = 0;
    public static final int Y = 1;
    public static final int ALPHA = 2;
    public static final int SCALE_X = 3;
    public static final int SCALE_Y = 4;

    public float mX = GLCoordinate.outSideWorldX();
    public float mY = GLCoordinate.outSideWorldY();
    public float mAlpha = 1.0f;
    public float mScaleX = 1.0f;
    public float mScaleY = 1.0f;

    public float x() {
        return mX;
    }

    public float y() {
        return mY;
    }

    public float alpha() {
        return mAlpha;
    }

    public float scaleX() {
        return mScaleX;
    }

    public float scaleY() {
        return mScaleY;
    }
}
