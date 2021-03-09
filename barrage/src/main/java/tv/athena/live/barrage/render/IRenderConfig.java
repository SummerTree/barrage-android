package tv.athena.live.barrage.render;

import tv.athena.live.barrage.render.draw.BulletBuilder;
import tv.athena.live.barrage.trace.AbsTrace;

import java.util.ArrayList;

/**
 * 弹幕区域需要用到的的接口
 *
 * @author donghao on 2018/8/28.
 */

public interface IRenderConfig<T extends AbsTrace> {

    BulletBuilder getShellBuilder();

    float getAlpha();

    float getScale();

    int getLineSpace();

    int getSpaceX();


    void addAnimation(T an);

    ArrayList<T> getAnimations();

    boolean isFixedQueue();

    int getFixedLine();

    void sendMsg(Object object);
}
