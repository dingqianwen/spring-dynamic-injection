package com.spring.dynamic.injection.config;

import com.spring.dynamic.injection.annotation.DynamicInjection;
import com.spring.dynamic.injection.exception.InjectionException;
import com.spring.dynamic.injection.proxy.InjectionProxy;
import com.spring.dynamic.injection.proxy.InjectionProxyClass;
import com.spring.dynamic.injection.proxy.InjectionProxyInterface;
import com.spring.dynamic.injection.util.Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "spring.dynamic.injection", name = "strategy", havingValue = "proxy")
@Slf4j
@Component
public class ProxyBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public ProxyBeanPostProcessor() {
        log.info("spring.dynamic.injection.strategy: proxy");
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
            boolean required = dynamicInjection.required();
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
                InjectionProxy injectionProxy;
                if (declaredField.getType().isInterface()) {
                    injectionProxy = new InjectionProxyInterface(this.applicationContext);
                } else {
                    injectionProxy = new InjectionProxyClass(this.applicationContext);
                }
                // 设置一个代理对象
                Object proxy = injectionProxy.createProxy(object, declaredField, name);
                declaredField.set(object, proxy);
            } catch (NoSuchBeanDefinitionException e) {
                if (required) {
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
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
