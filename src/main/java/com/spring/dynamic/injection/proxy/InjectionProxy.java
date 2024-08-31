package com.spring.dynamic.injection.proxy;

import java.lang.reflect.Field;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
public interface InjectionProxy {

    /**
     * 创建代理对象
     *
     * @param object        被代理对象
     * @param declaredField 字段
     * @param name          实现类配置
     * @return 代理对象
     */
    Object createProxy(Object object, Field declaredField, String name);

}
