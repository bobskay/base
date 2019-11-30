---
title: 初始化nacos
date: 2019-11-07 16:03:50
categories: [nacos]
tags: [nacos]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

初始化nacos

<!--more-->

下载并编译

```shell
git clone https://github.com/alibaba/nacos.git
cd nacos/
mvn -Prelease-nacos -DskipTests clean install -U
```

运行

```cmd
nacos\distribution\target\nacos-server-1.1.3\nacos\bin\startup.cmd
```

客户端