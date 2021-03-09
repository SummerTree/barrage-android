package tv.athena.live.barrage.render.area;

import tv.athena.live.barrage.GunPowder;
import tv.athena.live.barrage.config.BarrageConfig;
import tv.athena.live.barrage.render.IRenderConfig;
import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.trace.AbsTrace;

import java.util.Random;

/**
 * 闪烁弹幕
 */
public abstract class FlashArea extends AbsBarrageArea {
    public Random mRandom;

    public FlashArea(IRenderConfig barrage, int lineCount) {
        super(barrage, lineCount);
        mRandom = new Random();
    }

    @Override
    public boolean calculateBarrageReal(AbsTrace an) {
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
                        AbsTrace barrageAnimation = fire(bullet, 0.0f, 0.0f);
                        if (null != barrageAnimation) {
                            start(barrageAnimation, mBarrageHolder, last.mLineIndex);
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
            AbsTrace an = createBulletTrace(bullet, BarrageConfig.TYPE_FLASH);

            float posX = mRandom.nextInt(mRight) % (mRight - mLeft + 1) + mLeft;
            float posY = mRandom.nextInt(mBottom) % (mBottom - mTop + 1) + mTop;
            posX = posX + an.mWidth * mBarrageHolder.getScale() > mRight ? mRight - an.mWidth * mBarrageHolder.getScale() : posX;
            posY = posY + an.mHeight * mBarrageHolder.getScale() > mBottom ? mBottom - an.mHeight * mBarrageHolder.getScale() : posY;
            posX = posX < mLeft ? mLeft : posX;
            posY = posY > mBottom ? mTop : posY;

            an.alpha(0.0f, 1.0f).x(posX, posX).y(posY, posY).duration(bullet.getDuration())
                    .setRepeatCount(5).setRepeatModel(AbsTrace.REVERSE);
            an.scaleX(mBarrageHolder.getScale(), mBarrageHolder.getScale());
            an.scaleY(mBarrageHolder.getScale(), mBarrageHolder.getScale());


            return an;
        }

        return null;
    }

}
