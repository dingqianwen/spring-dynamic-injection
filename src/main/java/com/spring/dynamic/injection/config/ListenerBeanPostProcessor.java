package com.spring.dynamic.injection.config;

import com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent;
import com.spring.dynamic.injection.annotation.DynamicInjection;
import com.spring.dynamic.injection.exception.InjectionException;
import com.spring.dynamic.injection.util.Util;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@ConditionalOnMissingBean(ProxyBeanPostProcessor.class)
@Slf4j
@Component
public class ListenerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware
        , ApplicationListener<EnvironmentChangeEvent> {

    private ApplicationContext applicationContext;
    private Environment environment;
    /**
     * key为配置文件key，value为被DynamicInjection注解标记的属性
     */
    private final static Map<String, MapValue> FIELD_MAP = new HashMap<>();

    public ListenerBeanPostProcessor() {
        log.info("spring.dynamic.injection.strategy: listener");
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object object, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(object);
        for (Field declaredField : targetClass.getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(DynamicInjection.class)) {
                continue;
            }
            declaredField.setAccessible(true);
            // 如果已经有值，优先用户设置的值
            if (declaredField.get(object) != null) {
                continue;
            }
            DynamicInjection dynamicInjection = declaredField.getAnnotation(DynamicInjection.class);
            String name = dynamicInjection.value();
            try {
                // 当implClassName为空时，按照类型默认注入
                if (StringUtils.isEmpty(name)) {
                    declaredField.set(object, this.applicationContext.getBean(declaredField.getType()));
                    continue;
                }
                // 非动态表达式配置，不需要动态代理切换
                if (!Util.isExpression(name)) {
                    declaredField.set(object, this.applicationContext.getBean(name));
                    continue;
                }
                String var = name.substring(2, name.length() - 1);
                if (!StringUtils.hasLength(var)) {
                    throw new InjectionException("@DynamicInjection value config error : " + name);
                }
                String[] split = var.split(":");
                String configName = Util.get(split, 0);
                // 默认值
                String defaultValue = Util.get(split, 1);
                // 启动时，默认先读取配置注入一下
                String implClassName = this.environment.getProperty(configName, defaultValue);
                declaredField.set(object, this.applicationContext.getBean(implClassName));
                // 存储映射关系
                FIELD_MAP.put(configName, new MapValue(object, declaredField, defaultValue));
            } catch (NoSuchBeanDefinitionException e) {
                if (dynamicInjection.required()) {
                    throw e;
                }
                log.warn("No bean named '" + e.getBeanName() + "' available");
            } catch (Exception e) {
                throw new InjectionException(e);
            }
        }
        return object;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        doDynamicInjection(applicationContext, keys);
    }

    private static void doDynamicInjection(ApplicationContext applicationContext, Set<String> keys) {
        for (String key : keys) {
            if (!FIELD_MAP.containsKey(key)) {
                continue;
            }
            log.info("Dynamic injection, config changed : " + key);
            MapValue mapValue = FIELD_MAP.get(key);
            Field field = mapValue.getField();
            Object object = mapValue.getObject();
            if (object == null) {
                log.info("The original object has been destroyed, ignore it");
                continue;
            }
            String implClassName = applicationContext.getEnvironment().getProperty(key, mapValue.getDefaultClassName());
            Object bean = applicationContext.getBean(implClassName);
            try {
                field.set(object, bean);
            } catch (Exception e) {
                log.error("Dynamic injection fail", e);
            }
        }
    }


    @Slf4j
    @ConditionalOnClass(name = "com.ctrip.framework.apollo.spring.events.ApolloConfigChangeEvent")
    @Component
    public static class ApolloConfigListener implements ApplicationContextAware, ApplicationListener<ApolloConfigChangeEvent> {

        private ApplicationContext applicationContext;

        @Override
        public void onApplicationEvent(ApolloConfigChangeEvent apolloConfigChangeEvent) {
            Set<String> changedKeys = apolloConfigChangeEvent.getConfigChangeEvent().changedKeys();
            doDynamicInjection(applicationContext, changedKeys);
        }

        @Override
        public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }


    @Data
    @AllArgsConstructor
    public static class MapValue {
        private Object object;
        private Field field;
        private String defaultClassName;
    }

}
