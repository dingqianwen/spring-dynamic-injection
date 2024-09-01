# Spring多实现类动态注入

Spring中针对接口多实现类，在运行期间通过`Apollo`或者`Nacos`配置动态选择某个实现。

### 使用说明

项目中需要动态切换实现类的属性增加`@DynamicInjection`注解即可，使用方式如同：`@Resource`或`@Autowired`

```java

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ActiveProfiles("nacos")
public class AppTest {

    /**
     * 三个实现类如下：
     * -OrderServiceDbImpl
     * -OrderServiceEsImpl
     * -OrderServiceNoImpl
     * </p>
     * 默认使用：orderServiceEsImpl
     */
    @DynamicInjection(value = "${order-service.impl:orderServiceEsImpl}")
    private OrderService orderService;

    /**
     * 动态切换测试
     */
    @SneakyThrows
    @Test
    public void test() {
        while (true) {
            String result = this.orderService.query();
            log.info("调用完毕：" + result);
            Thread.sleep(2000);
        }
    }
}
```

`@DynamicInjection`注解有以下两个方法

| 方法       | 默认值  | 说明                                                                                                                                                                                                        |
|----------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| value    |      | 指定使用哪个实现类 `@DynamicInjection(value = "orderServiceEsImpl")` 等效于`@Resource`或者`@Autowired @Qualifier("orderServiceEsImpl")`，如果是`${}`的形式，直接从配置文件中获取，例如：`@DynamicInjection(value = "${order-service.impl:默认值}")` |
| required | `true` | 如果设置为`true`，启动时找不到`Bean`注入，抛出异常                                                                                                                                                                           |

### 实现思路

### 方案1

监听到配置文件变更时，主动为所有标记`@DynamicInjection`注解的属性注入最新配置的`Bean`

### 方案2（本项目实现）

项目启动时，为所有标记`@DynamicInjection`注解的属性注入一个动态代理对象，在目标方法出发时，即时获取到具体实现类。

```java
method.invoke(this.applicationContext.getBean(implClassName),args);
```

目前支持：接口多实现（JDK Proxy），多子类继承(CGLIB Proxy)