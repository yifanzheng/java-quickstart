package top.yifan.plugins;

import java.lang.annotation.*;

/**
 * Sensitive
 *
 * @author zhengyifan
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Sensitive {
    SensitiveType type();
}
