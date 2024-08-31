package com.spring.dynamic.injection.service;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
public interface OrderService {
    default String query() {
        return null;
    }
}
