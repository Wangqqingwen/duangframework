package com.duangframework.core.annotation.mvc;

/**
 * Created by laotang on 2017/11/5.
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义 Controller 类注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    // 设置Controller是单例还是多例模式, singleton: 单例  prototype: 多例
    String scope() default "singleton";
}
