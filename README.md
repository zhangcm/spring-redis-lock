## 基于Spring Boot和Redis实现的一个分布式锁工具包，简化方法的加锁操作。

### 背景
在项目中使用redis作为分布式锁来控制并发时，经常会看到如下代码:

```java
boolean locked = RedisUtil.tryLock(key);
try {
    if (locked) {
        // do business
    }
} finally {
    if (locked) {
        RedisUtil.unlock(key);
    }
}
```

这部分加锁的代码重复且冗余。如果项目中使用了JPA，并且上述代码包在一个带@Transaction的方法中，这部分代码还起不到加锁的作用。
为了简化开发，可以这部分代码作为aop代码抽取出来，就像Spring Transaction一样。

### 使用方法:

添加maven依赖:
```xml
<dependency>
    <groupId>io.github.zhangcm</groupId>
    <artifactId>spring-redis-lock</artifactId>
    <version>0.1.1-RELEASE</version>
</dependency>
```

在启动类加上`@EnableLock`注解，开启加锁功能

```java
@SpringBootApplication
@EnableLock
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class LockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockDemoApplication.class, args);
    }
}
```

在需要加锁的方法上，添加`@Lock`，通过`key`属性可直接指定要锁的key

```java
import com.justz.lock.annotation.Lock;
import org.springframework.stereotype.Service;

@Service
public class CountService {

    private int count = 0;

    @Lock(key = "count_lock")
    public void increWithLock() {
        count++;
    }

    public void increWithoutLock() {
        count++;
    }

    public int getCount() {
        return count;
    }

}
```

在`application.properties`中添加`redis`配置：
```
spring.redis.host=127.0.0.1
```

### 测试用例:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class CountServiceTest {

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private int total = 500;

    @Autowired
    private CountService countService;

    @Test
    public void testAddWithoutLock() throws InterruptedException {
        execute(total, countService::increWithoutLock);
        Assert.assertEquals(total, countService.getCount());
    }

    @Test
    public void testAddWithLock() throws InterruptedException {
        execute(total, countService::increWithLock);
        Assert.assertEquals(total, countService.getCount());
    }

    private void execute(int total, Runnable runnable) throws InterruptedException {
        for (int i = 0; i < total; i++) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(1000);
        }
    }
}
```

#### 测试结果：
* 第一个测试用例执行不通过，因为count++存在并发问题
* 第二个测试用例执行通过，用锁来保证count++的顺序执行

### 特性
* 自定义切入顺序。默认比@Transaction高1，确保在事务提交后再释放锁
* 自定义key的生成规则，需实现KeyGenerator接口
* 自定义锁的实现，需实现LockManager接口
* 自定义注解