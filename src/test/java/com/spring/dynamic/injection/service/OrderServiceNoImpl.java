package com.spring.dynamic.injection.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@Primary
@Service
public class OrderServiceNoImpl implements OrderService {
    @Override
    public String query() {
        return null;
    }
}
