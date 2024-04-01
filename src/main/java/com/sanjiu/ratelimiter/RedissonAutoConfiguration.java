package com.sanjiu.ratelimiter;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Redisson.class)
public class RedissonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(@Value("${spring.data.redis.host:localhost}") String host,
                                         @Value("${spring.data.redis.port:6379}") int port,
                                         @Value("${spring.data.redis.redisson.db:1}") int database) {
        Config config = new Config();
        // 此处简单处理，默认使用单机模式
        String redisUrl = "redis://" + host + ":" + port;
        config.useSingleServer()
                .setAddress(redisUrl)
                .setDatabase(database);
        return Redisson.create(config);
    }
}