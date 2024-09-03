package com.dingqw.spring.dynamic.injection.listener;

import com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;
import java.util.Set;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/1
 * @since 1.0.0
 */
@Slf4j
public class ApolloConfigListener implements ApplicationListener<ApolloConfigChangeEvent> {

    @Resource
    private DynamicInjectionListenerProcessor dynamicInjectionListenerProcessor;

    @Override
    public void onApplicationEvent(ApolloConfigChangeEvent apolloConfigChangeEvent) {
        Set<String> changedKeys = apolloConfigChangeEvent.getConfigChangeEvent().changedKeys();
        this.dynamicInjectionListenerProcessor.changedKeyDynamicInjection(changedKeys);
    }

}