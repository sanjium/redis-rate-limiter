package com.sanjiu.ratelimiter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author：三玖
 * @date: 2024/3/31
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimiter {
    int permits() default 3;

    long second() default 60L;

    String message() default "访问频次过快,请稍后再试";

}
