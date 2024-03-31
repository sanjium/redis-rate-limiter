package com.sanjiu.ratelimiter.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author：三玖
 * @date: 2024/3/31
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimiterException extends RuntimeException {

    /*
     * 错误信息
     * */
    private String msg;

    public static void cast(String msg) {
        throw new RateLimiterException(msg);
    }

}
