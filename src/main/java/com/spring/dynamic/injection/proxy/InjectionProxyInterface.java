package com.spring.dynamic.injection.proxy;

import com.spring.dynamic.injection.exception.InjectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@Slf4j
public class InjectionProxyInterface implements InjectionProxy {

    private final ApplicationContext applicationContext;
    private final Environment environment;

    public InjectionProxyInterface(ApplicationContext applicationContext) {
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
        return Proxy.newProxyInstance(object.getClass().getClassLoader(), new Class[]{declaredField.getType()},
                (proxy, method, args) -> {
                    String implClassName = this.environment.resolvePlaceholders(name);
                    if (StringUtils.isEmpty(implClassName)) {
                        throw new InjectionException("@DynamicInjection value config does not exist : " + name);
                    }
                    log.info("Dynamic injection impl class: {}", implClassName);
                    return method.invoke(this.applicationContext.getBean(implClassName), args);
                }
        );
    }

}
