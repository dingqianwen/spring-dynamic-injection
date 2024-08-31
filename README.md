# 延迟重试组件

### 项目产生原因

Spring已经提供@Retryable方法，增强方法增加重试逻辑，但是此方式如果中途断电，则导致后续重试丢失，无法保障业务数据一致性。

### 所需环境

需要配置RabbitMQ服务，并安装延迟队列插件。

### 项目使用方式

配置文件增加

```yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

项目中需要重试的方法增加 @DelayRetry 注解即可

```java
import com.delay.retry.annotation.EnableDelayRetry;

@EnableDelayRetry
@Service
public class PushDataService {

    /**
     * 推送用户数据
     *
     * @param user 用户数据
     */
    @DelayRetry(delayTime = "1000,2000,3000")
    public void pushUserData(UserEntity user) {
        System.out.println(user + "  " + new Date());
        // 模拟推送失败
        System.out.println(1 / 0);
    }
}
```

### @DelayRetry注解参数说明

#### queue：队列名称

允许自定义队列名称，默认类+方法（参数类型）

#### delayTime：延迟时间表达式

默认：`3000,5000,10000`
，表示方法执行失败后3秒后重试，依然失败5秒后重试，依然失败10秒后重试，依然失败执行fallback方法（如果有的话）。支持从配置文件中获取，例如：`@DelayRetry(delayTime = "${test.delay.time}")`

#### fallback：降级方法

当重试全部失败后执行此方法，落表兜底操作。执行回退方法时支持触发AOP代理，可以在方法上增加`@Async`、`@Transactional`等。

> 使用说明：方法的参数必须与被重试的方法参数保持一致，并且在同一个类中！

#### exclude：排除异常

排除某些异常不触发重试。
