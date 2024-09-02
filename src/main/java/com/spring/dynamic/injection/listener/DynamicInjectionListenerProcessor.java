package com.spring.dynamic.injection.listener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈〉
 *
 * @author dingqianwen
 * @date 2024/9/1
 * @since 1.0.0
 */
@Slf4j
public class DynamicInjectionListenerProcessor implements ApplicationContextAware {

    /**
     * key为配置文件key，value为当前key下所有被DynamicInjection注解标记的属性
     */
    private final static Map<String, List<MapValue>> FIELD_MAP = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;
    private Environment environment;

    /**
     * 相同key的Field放在一组
     *
     * @param key      配置的key
     * @param mapValue 对应的Field信息
     */
    public void add(String key, MapValue mapValue) {
        FIELD_MAP.computeIfAbsent(key, (k) -> new ArrayList<>()).add(mapValue);
    }

    public boolean containsKey(String key) {
        return FIELD_MAP.containsKey(key);
    }

    public List<MapValue> get(String key) {
        return FIELD_MAP.get(key);
    }

    /**
     * key改变时，刷新所有对应的Field
     *
     * @param keys 变更的key
     */
    public void changedKeyDynamicInjection(Set<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        if (CollectionUtils.isEmpty(FIELD_MAP)) {
            return;
        }
        for (String key : keys) {
            if (!this.containsKey(key)) {
                continue;
            }
            log.info("Dynamic injection, config changed : " + key);
            List<MapValue> mapValues = this.get(key);
            for (MapValue mapValue : mapValues) {
                Field field = mapValue.getField();
                Object object = mapValue.getObject();
                if (object == null) {
                    log.info("The original object has been destroyed, ignore it");
                    continue;
                }
                String implClassName = this.environment.getProperty(key, mapValue.getDefaultClassName());
                try {
                    Object bean = this.applicationContext.getBean(implClassName);
                    if (!Modifier.isPublic(field.getModifiers())) {
                        field.setAccessible(true);
                    }
                    field.set(object, bean);
                } catch (Exception e) {
                    log.error("Dynamic injection fail", e);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.environment = applicationContext.getEnvironment();
    }


    @Data
    @AllArgsConstructor
    public static class MapValue {
        private Object object;
        private Field field;
        private String defaultClassName;
    }
}
