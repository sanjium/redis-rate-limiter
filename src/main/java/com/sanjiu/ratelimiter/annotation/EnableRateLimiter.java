package com.sanjiu.ratelimiter.annotation;

import com.sanjiu.ratelimiter.RateLimiterPoint;
import com.sanjiu.ratelimiter.RedissonAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({RateLimiterPoint.class, RedissonAutoConfiguration.class})
public @interface EnableRateLimiter {
}
