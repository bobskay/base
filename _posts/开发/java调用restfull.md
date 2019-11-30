---
title: java调用restfull
date: 2019-10-05 09:53:28
categories: [java]
tags: [java,restfull]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

java调用restfull api操作的工具包

<!--more-->

# 主要功能

1. 根据dao的方法名和参数自动拼接url并根据相应的requestMapping调用具体的httprequest
2. 将返回的json转为对应的java对象

# 示例

对图书的curd操作

## 编写dao

```java
//配置此dao所使用的工厂类,不同的工厂类访问的基地址不同
@RestDao(TestConfig.BookstoreApiDaoFactory.class)
public interface BookDao {

    //新增或修改
    @PutMapping("/book/${book.id}")
    Book add(Book book);

    //删除
    @DeleteMapping("book/${id}")
    Book delete(String id);

    //通过id查询
    @GetMapping("/book/${id}")
    Book get(String id);

    //获取全部
    @GetMapping("/book/getAll")
    List<Book> getAll();
}
```

## spring配置类

```java
@Configuration
//RestfullDaoDefinitionRegistrar自动注册标记了RestDao的接口
@Import( {VelocityAutoConfiguration.class, JsonAutoConfiguration.class, RestfullDaoDefinitionRegistrar.class})
@ComponentScan("wang.wangby.bookstore")//扫描的包
public class TestConfig {

    public class BookstoreApiDaoFactory extends RestDaoFactory {
        public BookstoreApiDaoFactory(RestMethodInterceptor restMethodInterceptor) {
            super(restMethodInterceptor);
        }

    }

    //创建仓库dao
    @Bean
    public BookstoreApiDaoFactory registryDaoFactory(TemplateUtil templateUtil, JsonUtil jsonUtil) {
        SpecificRemoteHttpClient client=new SpecificRemoteHttpClient("http://myos:8080",new HttpConfig());
        RestMethodInterceptor interceptor=new RestMethodInterceptor(client,templateUtil,jsonUtil);
        return new BookstoreApiDaoFactory(interceptor);
    }
}
```

## 实际调用

```java
ApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
BookDao dao=context.getBean(BookDao.class);
Book book=dao.get("123");//执行这行代码的时会自动访问url:http://xxx/book/123,并将返回的json自动转为book对象
```

# 实现原理

## 注册RestDao

```java
//自动扫描带有RestDao的类并注册
public class RestfullDaoDefinitionRegistrar implements ImportBeanDefinitionRegistrar{
    ...
}
```

实现ImportBeanDefinitionRegistrar接口的类可以手动往spring容器中添加类，RestfullDaoDefinitionRegistrar

扫描系统所有类,找到类标签上标记了RestDao的类,将其添加到容器中

## 动态代理RestDao

### 用ProxyFactory实现动态代理

```java
public class RestDaoFactory implements InstantiationAwareBeanPostProcessor {
    //代理所有标记了RestDao的接口
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        RestDao restDao = AnnotationUtils.getAnnotation(beanClass, RestDao.class);
        if (restDao==null) {
            return null;
        }
        Class daofactroy=restDao.daoFactory();
        if(!this.getClass().getName().equals(daofactroy.getName())){
            return  null;
        }
        log.debug("找到bean{}->{},restDao={}" , beanName ,beanClass.getName(),restDao);
        ProxyFactory pf = new ProxyFactory();
        pf.setInterfaces(beanClass);
        pf.addAdvice(restMethodInterceptor);
        return pf.getProxy();
    }
}
```

spring容器启动创建bean的时候会查找所有实现InstantiationAwareBeanPostProcessor接口的类，然后调用postProcessBeforeInstantiation方法，如果该方法返回值不为null，就直接返回不走后面的创建过程了

### spring源码位置

org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInstantiation

```java
@Nullable
protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) {
    for (BeanPostProcessor bp : getBeanPostProcessors()) {
        if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
            if (result != null) {
                return result;
            }
        }
    }
    return null;
}
```









