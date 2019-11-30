---
title: rocketmq下载
date: 2018-08-04 00:01:10
categories: [rocketmq]
tags: [rocketmq]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

rocketmq下载

<!--more-->

# 下载

无法编译

1将rockedmq -all 下build下的plugin 全部包在 <pluginManagement> 下

2.将classifier注释

```xml
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-tcnative</artifactId>
    <version>1.1.33.Fork22</version>
    <!--<classifier>${os.detected.classifier}</classifier>-->
</dependency>
```

# 启动

nameserver:

```shell
e:
cd E:\app\rocketmq\rocketmq-all-4.2.0-bin-release\bin
mqnameserver
```

broker

```shell
e:
cd E:\app\rocketmq\rocketmq-all-4.2.0-bin-release\bin
mqbroker -c E:\opt\config\rocketmq\broker-a.properties
```



控制台

https://codeload.github.com/apache/rocketmq-externals/zip/master

```shell
cd E:\app\rocketmq\rocketmq-externals\rocketmq-console\target
java -jar rocketmq-console-ng-1.0.0.jar --server.port=7777  --rocketmq.config.namesrvAddr=127.0.0.1:9876
```





