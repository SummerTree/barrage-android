package tv.athena.live.barrage.view;

import tv.athena.live.barrage.BarrageEvent;
import tv.athena.live.barrage.GunPowder;

/**
 * 弹幕需要实现的基本接口
 *
 * @Author donghao on 2018/8/28.
 */

public interface IBarrageView extends IBarrageConfigView {

    /**
     * 弹幕开关
     *
     * @param isOpen
     */
    void switchRender(boolean isOpen);

    /**
     * 装填火药，发言弹幕
     */
    void offerGunPowder(GunPowder gunPowder, int type);

    /**
     * 停火
     */
    void ceaseFire(boolean cleanAll);

    /**
     * 透明度设置
     *
     * @param arg0
     */
    void onBarrageAlphaChanged(BarrageEvent.BarrageAlphaChanged arg0);


    /**
     * 字体大小设置
     *
     * @param arg0
     */
    void onBarrageSizeChanged(BarrageEvent.BarrageSizeChanged arg0);

    /**
     * 弹幕模式设置
     *
     * @param arg0
     */
    void onBarrageModelChanged(BarrageEvent.BarrageModelChanged arg0);

    /**
     * 是否有自定义top margin
     *
     * @return
     */
    boolean hasCustomMargin();

    void sendMsg(Object object);
}
