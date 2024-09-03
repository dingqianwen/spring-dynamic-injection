package com.dingqw.spring.dynamic.injection.service;

import com.dingqw.spring.dynamic.injection.annotation.DynamicInjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/1
 * @since 1.0.0
 */
@Slf4j
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Service
public class TestService {

    @DynamicInjection(value = "${order-service.impl}")
    private OrderService orderService;

    @DynamicInjection(value = "${order-service.impl}")
    private OrderService orderService2;

    public String test() {
        log.info("orderService2:" + orderService2.query());
        return this.orderService.query();
    }

}
