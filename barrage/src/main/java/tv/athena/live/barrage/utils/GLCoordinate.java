package tv.athena.live.barrage.utils;

/**
 * 坐标计算
 */
public final class GLCoordinate {
    public static final float TOP = 1.0f;
    public static final float BOTTOM = -TOP;
    public static final float NEAR = 1.0f;
    public static final float FAR = 2 * NEAR;
    public static final float EYE_Z = -NEAR;
    public static final float NEAR_Z = NEAR + EYE_Z;
    public static final float LEFT = -1.0f;
    public static final float RIGHT = -LEFT;

    private static final float GL_UNIT = Math.abs(GLCoordinate.TOP - GLCoordinate.BOTTOM);
    private static float WORLD_UNIT = GL_UNIT;
    private static float WORLD_WIDTH = 1.0f;
    private static float WORLD_HEIGHT = 1.0f;

    public static void setWorldSize(int width, int height) {
        WORLD_WIDTH = width;//Math.max(width, height);
        WORLD_HEIGHT = height;//Math.min(width, height);
        WORLD_UNIT = height;//Math.min(width, height);
    }

    public static float toGLUnit(float world) {
        return world * GL_UNIT / WORLD_UNIT;
    }

    public static float toGLPositionX(float worldX) {
        float pw = GL_UNIT * WORLD_WIDTH / WORLD_HEIGHT;
        return worldX * pw / WORLD_WIDTH - pw / 2.0f;
    }

    public static float toGLPositionY(float worldY) {
        return worldY * GL_UNIT / WORLD_UNIT - GL_UNIT / 2.0f;
    }

    public static float toWorldPositionX(float glX) {
        float pw = GL_UNIT * WORLD_WIDTH / WORLD_HEIGHT;
        return (glX + pw / 2.0f) * WORLD_WIDTH / pw;
    }

    public static float toWorldPositionY(float glY) {
        return (glY + GL_UNIT / 2.0f) * WORLD_UNIT / GL_UNIT;
    }

    public static float outSideWorldX() {
        return toGLPositionX(2.0f * WORLD_WIDTH);
    }

    public static float outSideWorldY() {
        return toGLPositionY(2.0f * WORLD_HEIGHT);
    }
}
