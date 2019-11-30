---
title: docker网络设置
date: 2018-08-30 07:44:24
categories: [docker]
tags: [docker]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

个人笔记

<!--more-->

本机ip 192.168.1.107

虚拟机ip 192.168.1.110



# 虚拟机操作

创建网络

```shell
docker network create --subnet=192.168.2.0/24 mynet
```



使用

```yaml
version: '3'
services:
   hello:
	container_name: hello
	networks:
        mynet:
          ipv4_address: 192.168.2.2
networks:
  mynet:
    external: true
```

查看结果

```shell
docker network inspect mynet
```



# 本机操作

windows增加路由规则，将add 192.168.2.1请求转到192.168.1.110

```shell
route add 192.168.2.1/24 192.168.1.114
```

# 问题

## 某些端口无法访问

查找新网卡名称 br-7cff62268b4c

```shell
[root@centos7 hello]# ip addr|grep 192.168.2
inet 192.168.2.1/24 scope global br-7cff62268b4c

```

设置iptables

```shell
 iptables -A DOCKER -d 192.168.2.1/24 ! -i br-7cff62268b4c -o br-7cff62268b4c -p tcp -m tcp --dport 8080 -j ACCEPT
```

命令记不住,可以导出以后配置修改

```shell
iptables-save -t filter > iptables.bak
```





