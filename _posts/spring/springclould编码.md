---
title: springcloud示例
date: 2019-10-07 15:25:41
categories: [springcloud]
tags: [springcloud]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

在搭建好的springcloud环境下编写微服务，主要是feign和hystrix的使用

spring版本号：2.1.9.RELEASE

clould版本号：Greenwich.SR3

<!--more-->

系统共有包含3个模块

bookstore-api：公共接口
bookstore-consumer：消费者
bookstore-provider：服务提供者

消费者和服务提供者都需要引入公共包

# 公共接口

## 引入feign

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

## 编写接口类

```java
@FeignClient(value="BOOKSTORE-PROVIDER",fallbackFactory = BookstoreApiFallback.class)
public interface BookstoreApi {
    @RequestMapping("/bookInfo/getBook")
    BookInfo getBook(@RequestParam("id") Long id);
}
```

BOOKSTORE-PROVIDER是服务提供方在eureka里显示的名称

![1570433669841](/blog/images/1570433669841.png)

fallbackFactory为异常处理类，当出现异常的时候会调用这个类返回的接口实现类进行处理

```java
@Component
public class BookstoreApiFallback  implements FallbackFactory<BookstoreApi> {
    @Override
    public BookstoreApi create(Throwable throwable) {
        return new BookstoreApi() {
            @Override
            public BookInfo getBook(Long id) {
                throw new RuntimeException("未知异常",throwable);
            }
        };
    }
}
```

# 服务提供方

## 引入eureka和Hystrix的jar包

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

## app.java

需要开启Hystrix并配置Hystrix相关的servlet

```java
@SpringBootApplication
@EnableHystrix
public class BookstoreProviderApp {
    public static void main(String args[]){
        SpringApplication.run(BookstoreProviderApp.class, args);
        
     @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/actuator/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```

实现公共部分里定义的接口

1）设置requestMapping保证最终生成url要和接口里定义的一样

2）在需要熔断方法上加上HystrixCommand，可以设置fallbackMethod失败后的回调方法

```java
@RequestMapping("/bookInfo")
public class BookInfoController extends BaseController implements BookstoreApi {
    ....
        
   	@RequestMapping("/getBook")
    @HystrixCommand(fallbackMethod = "getBookFail")
    public BookInfo getBook(Long id) {
        if(id==1){
            throw new RuntimeException("未知异常");
        }
        BookInfo bk=new BookInfo();
        bk.setBookId(id);
        bk.setBookName("调用成功,当前服务器:"+instanceId);
        return bk;
    }
}
```

超时设置为2秒

```properties
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=2000
```



# 消费者

## 需要eureka客户端和ribbon

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```

## app.java

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "wang.wangby.bookstore")
public class BookstoreConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(BookstoreConsumerApp.class, args);
    }
}

```

## 使用

当需要调用接口的时候直接注入接口类就可以了

```java
@Autowired
BookstoreApi bookstoreApi;
```

## 自定义路由规则

```java
@Slf4j
public class MyRule extends AbstractLoadBalancerRule  {
   //轮询
   public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        while (true){
            List<Server> upList = lb.getReachableServers();
            if(upList.size()==0){
                return null;
            }
            int i=count.incrementAndGet()%upList.size();
            Server server=upList.get(Math.abs(i));
            if(server.isAlive()){
                return server;
            }
            Thread.yield();
   }
}
```

