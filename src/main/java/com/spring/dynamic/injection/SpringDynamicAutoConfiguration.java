package com.spring.dynamic.injection;

import com.spring.dynamic.injection.config.DynamicInjectionBeanPostProcessor;
import com.spring.dynamic.injection.listener.ApolloConfigListener;
import com.spring.dynamic.injection.listener.DynamicInjectionListenerProcessor;
import com.spring.dynamic.injection.listener.EnvironmentChangeEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@Import(DynamicInjectionBeanPostProcessor.class)
@Configuration
public class SpringDynamicAutoConfiguration {

    @ConditionalOnClass(name = "com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent")
    @ConditionalOnMissingBean
    @Bean
    public ApolloConfigListener apolloConfigListener() {
        return new ApolloConfigListener();
    }


    @Bean
    public DynamicInjectionListenerProcessor dynamicInjectionListenerProcessor() {
        return new DynamicInjectionListenerProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public EnvironmentChangeEventListener environmentChangeEventListener() {
        return new EnvironmentChangeEventListener();
    }

}
