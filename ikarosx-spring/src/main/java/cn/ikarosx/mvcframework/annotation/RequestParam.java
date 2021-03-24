package cn.ikarosx.mvcframework.annotation;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    String value() default "";
}
