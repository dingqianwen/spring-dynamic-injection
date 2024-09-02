package com.spring.dynamic.injection.config;

import com.spring.dynamic.injection.annotation.DynamicInjection;
import com.spring.dynamic.injection.enums.DynamicInjectionStrategy;
import com.spring.dynamic.injection.exception.InjectionException;
import com.spring.dynamic.injection.listener.DynamicInjectionListenerProcessor;
import com.spring.dynamic.injection.proxy.InjectionProxy;
import com.spring.dynamic.injection.proxy.InjectionProxyClass;
import com.spring.dynamic.injection.proxy.InjectionProxyInterface;
import com.spring.dynamic.injection.util.Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
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
@Slf4j
public class DynamicInjectionBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {

    private final DynamicInjectionStrategy strategy;

    private ApplicationContext applicationContext;
    private Environment environment;
    private DynamicInjectionListenerProcessor dynamicInjectionListenerProcessor;

    public DynamicInjectionBeanPostProcessor(@Value("${spring.dynamic.injection.strategy:listener}")
                                             DynamicInjectionStrategy strategy) {
        log.info("spring.dynamic.injection.strategy: " + strategy.name());
        this.strategy = strategy;
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
                String var = name.substring(2, name.length() - 1);
                if (!StringUtils.hasLength(var)) {
                    throw new InjectionException("@DynamicInjection value config error : " + name);
                }
                switch (this.strategy) {
                    case PROXY: {
                        InjectionProxy injectionProxy;
                        if (declaredField.getType().isInterface()) {
                            injectionProxy = new InjectionProxyInterface(this.applicationContext);
                        } else {
                            injectionProxy = new InjectionProxyClass(this.applicationContext);
                        }
                        // 设置一个代理对象
                        Object proxy = injectionProxy.createProxy(object, declaredField, name);
                        declaredField.set(object, proxy);
                        break;
                    }
                    case LISTENER: {
                        String[] split = var.split(":");
                        String configName = Util.get(split, 0);
                        // 默认值
                        String defaultValue = Util.get(split, 1);
                        // 启动时，默认先读取配置注入一下
                        String implClassName = this.environment.getProperty(configName, defaultValue);
                        declaredField.set(object, this.applicationContext.getBean(implClassName));
                        // 存储映射关系
                        this.dynamicInjectionListenerProcessor.add(configName,
                                new DynamicInjectionListenerProcessor.MapValue(object, declaredField, defaultValue));
                    }
                }
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
        this.environment = applicationContext.getEnvironment();
        this.dynamicInjectionListenerProcessor = applicationContext.getBean(DynamicInjectionListenerProcessor.class);
    }


}
