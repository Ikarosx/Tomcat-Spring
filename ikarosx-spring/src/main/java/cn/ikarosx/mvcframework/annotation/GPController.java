package cn.ikarosx.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface GPController {
    String value() default "";
}
