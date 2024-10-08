package com.dingqw.spring.dynamic.injection.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 〈使用方式同@Resource、@Autowired〉
 *
 * @author dingqianwen
 * @date 2024/8/31
 * @since 1.0.0
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface DynamicInjection {

    /**
     * 指定使用哪个实现类 @DynamicInjection(value = "orderServiceEsImpl")
     * 等效于@Resource或者@Autowired @Qualifier("orderServiceEsImpl").
     * <p>
     * 如果是${}的形式，直接从配置文件中获取，
     * 例如：@DynamicInjection(value = "${order-service.impl:默认值}")
     *
     * @return 具体实现类，首字母小写，例如：userServiceImpl
     */
    String value() default "";

    /**
     * 如果设置为true，启动时找不到bean注入，抛出异常
     *
     * @return true
     */
    boolean required() default true;

}