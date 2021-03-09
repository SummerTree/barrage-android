package tv.athena.live.barrage.render.area;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.trace.AbsTrace;

/**
 * 上下（竖方向）方向弹幕
 */
public abstract class VerticalArea extends AbsBarrageArea {

    public VerticalArea(IRenderConfig barrage, int lineCount) {
        super(barrage, lineCount);
    }

    @Override
    public void setRect(int left, int top, int right, int bottom) {
        super.setRect(left, top, right, bottom);
        mRange = Math.abs(bottom - top);
    }

    @Override
    public boolean calculateBarrageReal(AbsTrace an) {
        if (toWorldPositionY(an.getCurrentFrame().y())
                + BarrageConfig.SpaceY + an.mHeight * an.getCurrentFrame().scaleY() < mRange) {

            if (null == mBarrageCache) {
                GunPowder ammo = mGunPowderQueue.poll();
                if (null != ammo) {
                    BulletBuilder.Bullet bullet = mBarrageHolder.getShellBuilder().gunPowderToBullet(ammo);
                    if (null != bullet) {
                        mBarrageCache = fire(bullet, InValid, 0.0f);
                    }
                }
            }

            if (null == mBarrageCache) {
                return false;
            }

            for (int i = 0; i < mLockers.size(); ++i) {
                if (mLockers.get(i)) {
                    continue;
                }

                float xPos = getAnimationPosX(mBarrageCache.mWidth, i);
                mBarrageCache.x(xPos, xPos);
                start(mBarrageCache, mBarrageHolder, i);
                mBarrageCache = null;
                return true;
            }

            float last = an.getDuration() - an.getCurrentTime();
            float need = mBarrageCache.getDuration() * (mRange - BarrageConfig.SpaceY)
                    / (mRange + mBarrageCache.mHeight * mBarrageHolder.getScale());
            if (last < need && an.mLineIndex < mLockers.size()) {
                float xPos = getAnimationPosX(an.mWidth, an.mLineIndex);
                mBarrageCache.x(xPos, xPos);
                start(mBarrageCache, mBarrageHolder, an.mLineIndex);
                mBarrageCache = null;
                an.mHasFollower = true;
            }
        }
        return true;
    }

    @Override
    protected AnimationListenerImpl createAnimationListener() {
        return new AnimationListenerImpl() {
            @Override
            protected void onLastItemEnd(AbsTrace last) {
                if (last.mLineIndex >= mLockers.size()) {
                    return;
                }

                GunPowder ammo = mGunPowderQueue.poll();
                if (null != ammo) {
                    BulletBuilder.Bullet bullet = mBarrageHolder.getShellBuilder().gunPowderToBullet(ammo);
                    if (null != bullet) {
                        float xPos = getAnimationPosX(last.mWidth, last.mLineIndex);
                        AbsTrace an = fire(bullet, xPos, 0.0f);
                        if (null != an) {
                            start(an, mBarrageHolder, last.mLineIndex);
                            return;
                        }
                    }
                }

                mLockers.set(last.mLineIndex, false);
            }
        };
    }

    @Override
    protected AbsTrace fire(BulletBuilder.Bullet bullet, float x, float y) {
        if (bullet.hasPixels()) {
            AbsTrace an = createBulletTrace(bullet, BarrageConfig.TYPE_VERTICAL);
            an.duration(bullet.getDuration());
            an.alpha(mBarrageHolder.getAlpha(), mBarrageHolder.getAlpha());

            if (InValid != x) {
                an.x(x, x);
            }

            return an;
        }

        return null;
    }

    private float getAnimationPosX(int widht, int lineIndex) {
        return (widht * mBarrageHolder.getScale() + BarrageConfig.COLUMN_SPACE) * lineIndex + mLeft;
    }

}
