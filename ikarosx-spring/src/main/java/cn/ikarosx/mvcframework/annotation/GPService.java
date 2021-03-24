package cn.ikarosx.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GPService {
    String value() default "";
}
