---
title: 常用sql
date: 2018-07-29 18:52:34
categories: [数据库,mysql]
tags: [mysql,sql]
typora-copy-images-to: ../blog/images/typora
typora-root-url:  E:/blog
---

一些mysql日常维护和使用经常用的sql，包括数据库，用户，表等

```sql
select user,host,password from mysql.user;
grant all privileges on *.* to ming@'*' identified by '123456';
update mysql.user set password=password('newpwd') where user="ming" and Host="%";
flush privileges;
select table_name ,column_name ,is_nullable ,data_type as datatype ,character_maximum_length ,column_comment 
from information_schema.columns  
where  table_schema='mysql' order by table_name, ordinal_position 
```

<!--more-->

# 查询数据库

```mysql
show databases;
show tables;
select user,host,password from mysql.user;
```

## mysql创建用户

```mysql
#创建用户ming，密码123456，所有数据库权限
CREATE USER 'ming'@'%' IDENTIFIED BY '123456';
grant all privileges on *.* to 'ming'@'%' ;
#新版mysql加密方式变了
ALTER USER 'ming'@'%' IDENTIFIED BY 'ming' PASSWORD EXPIRE NEVER
ALTER USER 'ming'@'%' IDENTIFIED WITH mysql_native_password BY 'ming'; 
flush privileges;

```

## mysql修改密码

```mysql
#注意第二句不写新密码不会生效
ALTER USER 'ming'@'%' IDENTIFIED WITH mysql_native_password BY 'ming'; 
flush privileges;
```

## mysql删除用户

直接删mysql.user表记录就好了

# 查询表结构

```mysql
#查询表名和备注
select table_name,table_comment from information_schema.TABLES where table_schema='mysql'
#查看字段信息
select table_name ,column_name ,is_nullable ,data_type as datatype ,character_maximum_length ,column_comment 
from information_schema.columns  
where  table_schema='mysql' order by table_name, ordinal_position 
```



