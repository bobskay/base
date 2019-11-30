---
title: Pointcut配置
date: 2018-08-02 23:08:13
categories: [code,spring]
tags: [aop]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

Pointcut配置，[官方文档](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-ataspectj)

<!--more-->

# 名称

..代表任意包。第一个\*是返回值，第二个\*是类名,第三个\*是方法名

```java
 @Pointcut("execution(* com.xyz.someapp..service.*.*(..))")
    public void businessService() {}
```

# 注解

所有类注解包含Transactional

```java
@within(org.springframework.transaction.annotation.Transactional)
```

方法上包含注解Transactional

```java
@annotation(org.springframework.transaction.annotation.Transactional)
```
# 参数

参数是特定类型

```java
args(com.my.model.Request)
```

# 与或非

&& ||  !

```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {}

@Pointcut("within(com.xyz.someapp.trading..*)")
private void inTrading() {}

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {}
```
