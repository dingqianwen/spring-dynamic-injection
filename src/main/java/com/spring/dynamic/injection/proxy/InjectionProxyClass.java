package com.spring.dynamic.injection.proxy;

import com.spring.dynamic.injection.exception.InjectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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
public class InjectionProxyClass implements InjectionProxy {

    private final ApplicationContext applicationContext;
    private final Environment environment;

    public InjectionProxyClass(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.environment = this.applicationContext.getEnvironment();
    }


    /**
     * 创建代理对象
     *
     * @param object        被代理对象
     * @param declaredField 字段
     * @param name          实现类配置
     * @return 代理对象
     */
    @Override
    public Object createProxy(Object object, Field declaredField, String name) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(declaredField.getType());
        enhancer.setCallback((MethodInterceptor) (obj, method, args, methodProxy) -> {
            String implClassName = this.environment.resolvePlaceholders(name);
            if (StringUtils.isEmpty(implClassName)) {
                throw new InjectionException("@DynamicInjection value config does not exist : " + name);
            }
            log.info("Dynamic injection sub class: {}", implClassName);
            return method.invoke(this.applicationContext.getBean(implClassName), args);
        });
        return enhancer.create();
    }

}
