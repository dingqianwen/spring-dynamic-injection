# Spring多实现类动态注入

Spring中针对接口多实现类，在运行期间通过Apollo或者Nacos配置动态选择某个实现。


### 使用说明

项目中需要动态切换实现类的属性增加`DynamicInjection`注解即可

```java
@DynamicInjection(value = "${order-service.impl:orderServiceEsImpl}")
private OrderService orderService;

@Test
public void test() {
    String result = this.orderService.query();
    log.info("调用完毕：" + result);
}
```
