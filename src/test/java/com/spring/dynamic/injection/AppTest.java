package com.spring.dynamic.injection;

import com.spring.dynamic.injection.annotation.DynamicInjection;
import com.spring.dynamic.injection.service.OrderService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * Unit test for simple App.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ActiveProfiles("apollo")
public class AppTest {

    @DynamicInjection(value = "${order-service.impl}")
    private OrderService orderService;
    @Value("${order-service.impl:}")
    private String value;

    @SneakyThrows
    @Test
    public void test() {
        log.info("配置：" + value);
        while (true) {
            String result = this.orderService.query();
            log.info("调用完毕：" + result);
            Thread.sleep(2000);
        }
    }

    @Before
    public void bef() {
        MDC.put("requestId", UUID.randomUUID().toString());
    }

}
