package tv.athena.live.barrage.view;

import tv.athena.live.barrage.render.IBarrageRender;

/**
 * @author donghao on 2018/9/6.
 */

public interface IBarrageViewController extends IBarrageView {

    IBarrageRender getRender();

    /**
     * 绘制弹幕
     *
     * @return 绘制时间
     */
    long drawDanmakus();

    /**
     * view 是否准备好
     *
     * @return true/false
     */
    boolean isViewReady();

    /**
     * 清理view canvas
     */
    void clearCanvas();

    float getFps();
}
