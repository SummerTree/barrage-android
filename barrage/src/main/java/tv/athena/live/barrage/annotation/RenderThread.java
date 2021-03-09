package tv.athena.live.barrage.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author mylhyz 2019/1/13
 * 在render线程调用
 */
@Retention(RetentionPolicy.SOURCE)
public @interface RenderThread {
}
