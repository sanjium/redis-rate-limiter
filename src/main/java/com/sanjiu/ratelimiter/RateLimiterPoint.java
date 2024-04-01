package com.sanjiu.ratelimiter;

import cn.dev33.satoken.stp.StpUtil;
import com.sanjiu.ratelimiter.annotation.OverallRateLimiter;
import com.sanjiu.ratelimiter.annotation.RateLimiter;
import com.sanjiu.ratelimiter.exception.RateLimiterException;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author：三玖
 * @date: 2024/3/31
 */
@Component
@Aspect
public class RateLimiterPoint {

    @Resource
    private RedissonClient redissonClient;


    @Pointcut("@annotation(com.sanjiu.ratelimiter.annotation.RateLimiter)")
    public void rateLimiterPt() {
    }

    @Pointcut("@annotation(com.sanjiu.ratelimiter.annotation.OverallRateLimiter)")
    public void overallRateLimiterPt() {
    }

    @Around("rateLimiterPt()")
    public Object rateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获取方法上的@RateLimiter注解实例
        RateLimiter rateLimiter = methodSignature.getMethod().getAnnotation(RateLimiter.class);
        int permits = rateLimiter.permits();
        long second = rateLimiter.second();
        String message = rateLimiter.message();
        long userId = Long.parseLong(StpUtil.getLoginId().toString());
        String clazzName = joinPoint.getTarget().getClass().getName();
        String methodName = methodSignature.getMethod().getName();
        String key = "rateLimiter:" + clazzName + ":" + methodName + ":" + userId;

        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        boolean isSet = rRateLimiter.trySetRate(RateType.OVERALL, permits, second, RateIntervalUnit.SECONDS);
        // 设置限流器在一定时间内过期，比如30分钟
        if (isSet) {
            rRateLimiter.expire(30, TimeUnit.MINUTES);
        }
        if (rRateLimiter.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            throw new RateLimiterException(message);
        }
    }

    @Around("overallRateLimiterPt()")
    public Object overallRateLimiter(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获取方法上的@RateLimiter注解实例
        OverallRateLimiter rateLimiter = methodSignature.getMethod().getAnnotation(OverallRateLimiter.class);
        int permits = rateLimiter.permits();
        long second = rateLimiter.second();
        String message = rateLimiter.message();
        String clazzName = joinPoint.getTarget().getClass().getName();
        String methodName = methodSignature.getMethod().getName();
        String key = "overallRateLimiter:" + clazzName + ":" + methodName;

        RRateLimiter rRateLimiter = redissonClient.getRateLimiter(key);
        boolean isSet = rRateLimiter.trySetRate(RateType.OVERALL, permits, second, RateIntervalUnit.SECONDS);
        // 设置限流器在一定时间内过期，比如30分钟
        if (isSet) {
            rRateLimiter.expire(30, TimeUnit.MINUTES);
        }
        if (rRateLimiter.tryAcquire()) {
            return joinPoint.proceed();
        } else {
            throw new RateLimiterException(message);
        }
    }


}
