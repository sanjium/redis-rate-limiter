package com.sanjiu.ratelimiter;

import cn.dev33.satoken.stp.StpUtil;
import com.sanjiu.ratelimiter.annotation.RateLimiter;
import com.sanjiu.ratelimiter.exception.RateLimiterException;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
    private RedisTemplate<String, Object> redisTemplate;


    @Pointcut("@annotation(com.sanjiu.ratelimiter.annotation.RateLimiter)")
    public void rateLimiterPt() {
    }

    @Pointcut("@annotation(com.sanjiu.ratelimiter.annotation.OverAllRateLimiter)")
    public void overAllRateLimiterPt() {
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
        // 循环尝试获取锁
        while (true) {
            // 尝试获取锁
            Boolean isLock = redisTemplate.opsForValue().setIfAbsent(key + "lock", Thread.currentThread().getId(), second, TimeUnit.SECONDS);
            if (isLock) {
                try {
                    Long count;
                    if (redisTemplate.opsForValue().get(key) == null) {
                        // 计数器不存在，设置初始值并设置过期时间
                        count = 1L;
                        redisTemplate.opsForValue().set(key, count, second, TimeUnit.SECONDS);
                    } else {
                        // 计数器已存在，递增并设置过期时间
                        count = redisTemplate.opsForValue().increment(key);
                    }

                    // 检查计数器是否超过限制
                    if (count > permits) {
                        // 超过限制，拒绝请求
                        throw new RateLimiterException(message);
                    }

                    // 允许请求
                    return joinPoint.proceed();
                } finally {
                    redisTemplate.delete(key + "lock");
                }
            } else {
                // 等待一段时间后继续尝试获取锁
                Thread.sleep(100); // 等待10毫秒后重试
            }
        }
    }


}
