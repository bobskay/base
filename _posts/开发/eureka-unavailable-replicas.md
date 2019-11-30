---
title: eureka集群unavailable-replicas问题
date: 2018-07-29 13:47:25
categories: [code,spring,eureka]
tags: [eureka]
typora-copy-images-to: ../blog/images/
typora-root-url:  E:/blog
---

搭建了2台eureka的集群，结果一直处于unavailable-replicas 

<!--more-->

![1532843798177](/blog/images//1532843798177.png)

host文件已经做了域名映射

```properties
	127.0.0.1       eureka1
	127.0.0.1       eureka2
```

并且两台机器都显式开启了注册

```properties
eureka.client.fetch-registry=true
eureka.client.register-with-eureka=true
```

查了半天发现还要设置hostname,最终的配置如下

eureka1

```yaml
server:
  port: 7001
      
eureka:
  client:
    service-url:
      defaultZone: http://eureka2:7002/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance: 
    hostname: eureka1
```

eureka2

```yaml
server:
  port: 7002
      
eureka:
  client:
    service-url:
      defaultZone: http://eureka1:7001/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance: 
    hostname: eureka2
    prefer-ip-address: false
```

prefer-ip-address的作用是将点开的服务器连接换成IP。对于Eureka服务来说，如果eureka部署在同一台机器，并且这个值设成了true，也会造成unavailable-replicas，即使host做了ip映射，hostname也写了也没用。

![1532844837241](/blog/images//1532844837241.png)

