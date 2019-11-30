---
title: centos7下安装mysql
date: 2018-07-29 18:10:28
categories: [数据库,mysql]
tags: [mysql]
typora-copy-images-to: ../blog/images/typora
typora-root-url:  E:/blog
---

coentos7安装mysql

```shell
wget http://repo.mysql.com/mysql-community-release-el7-5.noarch.rpm
rpm -ivh mysql-community-release-el7-5.noarch.rpm
yum -y install mysql-server
systemctl start mysqld.service
mysql -u root
grant all privileges on *.* to ming@'%' identified by 'ming';
```

<!--more-->

# 安装

```shell
wget http://repo.mysql.com/mysql-community-release-el7-5.noarch.rpm
rpm -ivh mysql-community-release-el7-5.noarch.rpm
yum -y install mysql-server
```

看到这个说明就在下载了，如果没下载，到别处看看吧，这篇文章可能过时了

![1532859581622](/blog/images/1532859581622.png)

如果发现yum被锁,ps -ef|grep yum |awk '{print $1}'|kill-9 

# 启动

```shell
systemctl start mysqld.service
```

输入mysql能进去就说明启动成功了

![1532860151146](/blog/images/1532860151146.png)

# 初始化数据库

```mysql
mysql -u root
create database hello;
grant all privileges on hello.* to foo@localhost identified by '123456';
#如果不限制IP改为
#grant all privileges on hello.* to foo@'%' identified by '123456';
```

![1532861056129](/blog/images/1532861056129.png)

