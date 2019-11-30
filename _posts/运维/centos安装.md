---
title: 虚拟机
date: 2019-02-03 10:33:55
categories: [初始化]
tags: [centos,linux,visualbox]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

visualbox安装

<!--more-->

目录

```shell
#虚拟机根目录
E:\visualbox
#最小化centos备份
E:\visualbox\centosmin.bak
```

![1549344322441](/blog/images/1549344322441.png)

# 初始化

## 账号

root:root

ming:ming



## 远程登录

```shell
ip addr|grep 192.168
```

# 设置yum源

```shell
#直接删除,不备份了
rm -rf /etc/yum.repos.d/*
cp /media/sf_linux/etc/yum.repos.d/* /etc/yum.repos.d/
#修改内容为E:\linux\Centos-7.repo
#更新 yum update 可能比较久
yum clean all 
yum makecache 
yum update -y
```

# 安装visualbox增强工具

更新内核

```shell

#安装一些该工具包
yum -y install vim
yum -y install lrzsz 
yum -y install net-tools
yum -y install lsof
#根据/media/VBoxLinuxAdditions.run的执行结果提示,安装所需部件
yum -y install gcc
yum install kernel-devel gcc -y
yum install bzip2 -y
#重启
reboot
```

安装VBoxGuestAdditions

```shell
#将E:\linux\VBoxGuestAdditions.iso拖到/root/下
#挂载
mount /root/VBoxGuestAdditions.iso /media/
#mount: /dev/loop0 写保护，将以只读方式挂载
#安装增强工具
/media/VBoxLinuxAdditions.run
```

分配共享目录

![1549161901114](/blog/images/1549161901114.png)

挂载共享目录

```shell
#重启后自动挂载到/media/sf_linux
#创建目标目录
#mkdir /win
#-t 文件系统
#mount -t vboxsf linux /win
```



# 复制

将E:\visualbox\bak\centosmin.vdi复制到E:\visualbox ,改名node01.kube.wang.vdi

新建时选择刚才复制的文件

![1549166985981](/blog/images/1549166985981.png)

设置网络为桥接

![1549166537511](/blog/images/1549166537511.png)

远程登录

```shell
#重启网络
systemctl restart network
#查看ip
ip addr|grep 192.168
```

命令行启动

```shell
systemctl set-default multi-user.target
```



设置hostname

```shell
hostnamectl set-hostname node2
```

修改为静态ip

```shell
#192.168.1.201为要设置的ip
#需确认网络已经设置为桥接，网卡名称如果不是enp0s3，需要删除原来配置，并修改文件里的NAME和DEVICE
#重启
cat /media/sf_linux/etc/sysconfig/network-scripts/ifcfg-enp0s3 | sed  "s/192.168.1.200/192.168.1.114/" > /etc/sysconfig/network-scripts/ifcfg-enp0s3
systemctl restart network
```

修改启动命令

```shell
vim ~/.bashrc
```





ifcfg-enp0s3内容

```properties
TYPE="Ethernet"
PROXY_METHOD="none"
BROWSER_ONLY="no"

#$BOOTPROTO="dhcp"
BOOTPROTO="static"
IPADDR=192.168.1.200
NETMASK=255.255.255.0
GATEWAY=192.168.1.1
DNS1=8.8.8.8

DEFROUTE="yes"
IPV4_FAILURE_FATAL="no"
IPV6INIT="yes"
IPV6_AUTOCONF="yes"
IPV6_DEFROUTE="yes"
IPV6_FAILURE_FATAL="no"
IPV6_ADDR_GEN_MODE="stable-privacy"
NAME="enp0s3"
UUID="b6317c3a-28b1-4d40-a097-d8e63f285948"
DEVICE="enp0s3"
ONBOOT="yes"
```



<!--more-->

