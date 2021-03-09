package tv.athena.live.barrage.view;

/**
 * OpenGL弹幕业务上需要实现的额外接口
 *
 * @Author donghao on 2018/7/27.
 */

public interface IGLBarrageView extends IBarrageView {

    /**
     * 是否打开弹幕遮罩
     *
     * @return
     */
    boolean isStencilEnable();

    /**
     * 是否需要清理，主要用于8.0弹幕残影适配
     *
     * @return
     */
    boolean isNeedClearEnable();

    /**
     * 显示toast
     *
     * @param content
     */
    void showToast(String content);

}
