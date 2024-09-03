package com.dingqw.spring.dynamic.injection.exception;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
public class InjectionException extends RuntimeException {
    public InjectionException(String message) {
        super(message);
    }

    public InjectionException(Exception exception) {
        super(exception);
    }
}
