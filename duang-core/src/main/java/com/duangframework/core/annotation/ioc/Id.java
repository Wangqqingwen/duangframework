package com.duangframework.core.annotation.ioc;

import java.lang.annotation.*;

/**
 * Created by laotang on 2017/11/16.
 */
@Target({ElementType.FIELD,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Id {
    String value() default "";
}
