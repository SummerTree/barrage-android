package tv.athena.live.barrage.render.area;

import tv.athena.live.barrage.trace.AbsTrace;

import java.lang.ref.WeakReference;

public class AnimationListenerImpl implements OnAnimationListener {

    private WeakReference<AbsTrace> mLastTarget = null;

    public void setTarget(AbsTrace target) {
        if (null != mLastTarget) {
            AbsTrace tag = mLastTarget.get();
            if (null != tag) {
                tag.setListener(null);
            }
        }

        mLastTarget = new WeakReference<>(target);
    }

    @Override
    public void onAnimationEnd(AbsTrace animation) {
        AbsTrace last = mLastTarget.get();

        if (null != last) {
            onLastItemEnd(last);
        }
    }

    protected void onLastItemEnd(AbsTrace last) {

    }
}
