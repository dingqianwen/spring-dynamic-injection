package com.spring.dynamic.injection.listener;

import org.springframework.beans.BeansException;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/1
 * @since 1.0.0
 */
@Component
public class EnvironmentChangeEventListener implements ApplicationContextAware,
        ApplicationListener<EnvironmentChangeEvent> {

    private DynamicInjectionListenerProcessor dynamicInjectionListenerProcessor;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        this.dynamicInjectionListenerProcessor.changedKeyDynamicInjection(keys);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.dynamicInjectionListenerProcessor = applicationContext.getBean(DynamicInjectionListenerProcessor.class);
    }

}
