package cn.ikarosx.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.FIELD)
public @interface GPAutowired {
    String value() default "";
}
