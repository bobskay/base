---
title: springcloud环境搭建
date: 2018-07-30 18:01:30
categories: [code,spring]
tags: [springcloud，微服务]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

通过springcloud环境搭建，包括spring-config-server，eureka，hystrix

spring版本号：2.1.9.RELEASE

clould版本号：Greenwich.SR3

<!--more-->

# spring-config-server

## 创建服务

### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

### app.java

```java
@SpringBootApplication
@EnableConfigServer
public class MserviceConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MserviceConfigServerApplication.class, args);
    }
}
```

## 客户端配置

### 添加bootstrap.yml

```yaml
spring:
  cloud:
    config:
      uri: http://192.168.2.100:9001/config/
  application:
    name: mservice-eureka-server
```

### 添加jar包

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

## 使用

客户端可以通过以下方式获取配置值,config是自定义的前缀，在configserver里的spring.cloud.config.server.prefix=config

```http
/config/{name}-{profiles}.properties
/config/{name}-{profiles}.yml
/config/{name}-{profiles}.yaml
/config/{name}/{profiles:.*[^-].*}
/config/{label}/{name}-{profiles}.json
/config/{label}/{name}-{profiles}.properties
/config/{name}-{profiles}.json
/config/{label}/{name}-{profiles}.yml
/config/{label}/{name}-{profiles}.yaml
/config/{name}/{profiles}/{label:.*}
```

配置中心最终将从容器中获取EnvironmentRepository的实现类并调用findOne获取配置信息覆盖本地的application.yml内容

```java
org.springframework.cloud.config.server.environment.EnvironmentRepository#findOne
```

目前配置目前是写在数据库里的

```sql
DROP TABLE IF EXISTS `m_appconfig`;

CREATE TABLE `m_appconfig` (
  `appConfigId` double DEFAULT NULL,
  `application` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `profile` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `varLabel` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `varKey` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `varValue` varchar(255) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


insert  into `m_appconfig`(`appConfigId`,`application`,`profile`,`varLabel`,`varKey`,`varValue`) values (7,NULL,NULL,NULL,'logging.level.wang.wangby','debug'),(8,NULL,NULL,NULL,'logging.pattern.console','%-5level %logger{0}.%M:%L - %msg%n'),(14,NULL,NULL,NULL,'eureka.instance.prefer-ip-address','false'),(15,NULL,NULL,NULL,'eureka.client.service-url.defaultZone','http://eureka1:7000/eureka/,http://eureka2:7000/eureka/,http://eureka3:7000/eureka/'),(20,NULL,NULL,NULL,'mybatis.mapper-locations','classpath:mybatis/mapper/**/*.xml'),(16,'mservice-demo-client',NULL,'demo1','server.port','8080'),(13,'mservice-demo-server',NULL,'demo1','mserver.machineNo','101'),(17,'mservice-demo-server',NULL,'demo1','server.port','8001'),(18,'mservice-demo-server',NULL,'demo2','server.port','8002'),(19,'mservice-demo-server',NULL,'demo2','mserver.machineNo','102'),(9,'mservice-demo-server',NULL,'demo\\d','spring.datasource.username','root'),(10,'mservice-demo-server',NULL,'demo\\d','spring.datasource.password','123456'),(11,'mservice-demo-server',NULL,'demo\\d','spring.datasource.driver-class-name','com.mysql.jdbc.Driver'),(12,'mservice-demo-server',NULL,'demo\\d','spring.datasource.url','jdbc:mysql://127.0.0.1:3306/demo?useUnicode=true&charact&useSSL=false'),(1,'mservice-eureka-server',NULL,NULL,'eureka.client.fetch-registry','true'),(2,'mservice-eureka-server',NULL,NULL,'eureka.client.register-with-eureka','true'),(4,'mservice-eureka-server',NULL,'1','eureka.client.service-url.defaultZone','http://eureka2:7000/eureka/,http://eureka3:7000/eureka/'),(5,'mservice-eureka-server',NULL,'2','eureka.client.service-url.defaultZone','http://eureka1:7000/eureka/,http://eureka3:7000/eureka/'),(6,'mservice-eureka-server',NULL,'','server.port','7000'),(21,'mservice-hystrix-dashboard',NULL,NULL,'server.port','9002'),(22,'bookstore-provider',NULL,NULL,'mserver.machineNo','200'),(23,'bookstore-provider',NULL,'1','eureka.instance.instance-id','bookstoreProvider1'),(24,'bookstore-provider',NULL,'2','eureka.instance.instance-id','bookstoreProvider2'),(25,'mservice-eureka-server',NULL,'3','eureka.client.service-url.defaultZone','http://eureka1:7000/eureka/,http://eureka2:7000/eureka/'),(27,'mservice-eureka-server',NULL,'1','eureka.instance.hostname','eureka1'),(28,'bookstore-provider',NULL,'3','eureka.instance.instance-id','bookstoreProvider3');
```

# eureka

## 创建服务

### pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

### app.java

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
	public static void main(String[] args) throws ClassNotFoundException {
		SpringApplication.run(EurekaServerApplication.class, args);
	}
}
```

## 从配置中心读取到的配置

```yaml
eureka:
  client:
    fetch-registry: 'true'
    register-with-eureka: 'true'
    service-url:
      defaultZone: http://eureka1:7000/eureka/,http://eureka2:7000/eureka/
  instance:
    hostname: eureka1
    prefer-ip-address: 'false'
server:
  port: '7000'
spring:
  application:
    name: mservice-eureka-server
```

## 可能问题

如果出现unavailable-replicas需要将prefer-ip-address设为false

# hystrix-dashboard

## pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

## app.java

```java
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashboardApplication {
	public static void main(String[] args) throws ClassNotFoundException {
		SpringApplication.run(HystrixDashboardApplication.class, args);
	}
}
```

## 访问地址

```http
http://hystrixdashboard:9002/hystrix
```

![1570441481579](/blog/images/1570441481579.png)

地址栏输入要监控的服务提供者

## 最终效果

![1570441678461](/blog/images/1570441678461.png)