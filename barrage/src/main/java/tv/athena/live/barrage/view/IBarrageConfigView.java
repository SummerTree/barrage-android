package tv.athena.live.barrage.view;

/**
 * @author mylhyz 2019/1/21
 */
public interface IBarrageConfigView {

    /**
     * 是否限制队列尺寸
     *
     * @return true/false
     */
    boolean isQueueFixed();

    /**
     * 限制队列行数
     *
     * @return 数值
     */
    int getQueueLine();
}
