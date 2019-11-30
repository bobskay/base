---
title: zabbix使用
date: 2018-07-30 18:01:30
categories: [ops,monitor]
tags: [zabbix]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

安装zabbix并添加自定义监控，通过模板实现监控项的自动添加

<!--more-->

# 安装

## yum源方式

```
rpm -i https://repo.zabbix.com/zabbix/3.4/rhel/7/x86_64/zabbix-release-3.4-2.el7.noarch.rpm
yum install zabbix-server-mysql zabbix-web-mysql zabbix-agent
```

### 初始化数据库

```
# mysql -uroot -p
password
mysql> create database zabbix character set utf8 collate utf8_bin;
mysql> grant all privileges on zabbix.* to zabbix@localhost identified by 'password';
mysql> quit;
```

### 导入数据

```
# zcat /usr/share/doc/zabbix-server-mysql*/create.sql.gz | mysql -uzabbix -p zabbix
```

### 配置zabbix_serve

/etc/zabbix/zabbix_server.conf

```
DBPassword=password
```

### 修改php

```
# php_value date.timezone Europe/Riga
```

### 启动

```
# systemctl restart zabbix-server zabbix-agent httpd
# systemctl enable zabbix-server zabbix-agent httpd
```

## ISO方式

- Linux Ubuntu用户名及密码：
  appliance/zabbix
  root没有初始密码，sudo passwd root可配置root密码
- zabbix web用户名及密码
  Admin/zabbix

# mysql

## 启动

systemctl start mysqld.service

## 查询登录密码

grep 'temporary password' /var/log/mysqld.log

## 修改密码政策

set global validate_password_policy=0;

set global validate_password_length=1;

## 设置密码

SET PASSWORD = PASSWORD('123456');



# 测试

yum install zabbix-get

zabbix_get -s 127.0.0.1 -p 10050 -k "system.cpu.load[all,avg1]"

## 无数据

检查服务是否启动

lsof -i:10051

查看日志

/var/log/zabbix/zabbix_server

##  Permission denied

curl: (7) Failed to connect to 192.168.1.103: Permission denied

关闭Selinux

setenforce 0

# 常用命令

## 重启

service zabbix-server restart 

systemctl restart zabbix-agent.service

# 优化

改为主动式

配置主机名称和和zabbix_agentd.conf中的Hostname配置一样  

# 自定义配置

## zabbix发现流程

![img](/blog/images/clipboard.png) 

##  提供接口

发现接口

```java
@Query(name="提供给zabbix用于自动发现的接口",path="zabbix")
public String zabbix(String host,Integer port){
    List list=new ArrayList();
    TimeMonitor tm=(TimeMonitor) MonitorFacade.getMonitor(MonitorFacade.HTTP_MONITOR);
    tm.getMap().forEach((name,st)->{
        Map map=new LinkedHashMap();
        for(int i=0;i<st.getAccuracy().length;i++) {
            map.put("{#ITEM"+i+"}",st.getAccuracy()[i]);
        }
        map.put("{#NAME}",name);
        map.put("{#URL}", "/app/monitor/getItem");
        list.add(map);
    });
    Map map=new HashMap();
    map.put("data", list);
    return jsonUtil.toString(map);
}
```

取值接口

```java
@Query(name="获取单个统计项的值",path="getItem")
public String getItem(String name,String item){
    try {
        TimeMonitor tm=(TimeMonitor) MonitorFacade.getMonitor(MonitorFacade.HTTP_MONITOR);
        TimeStatist st=tm.get(name);
        if(st==null) {
            log.info("找不到监控项:"+name);
            return "0";
        }
        int num=Integer.parseInt(item);
        return st.getAccuracyCount()[num].get()+"";
    }catch(Exception ex) {
        ex.printStackTrace();
        log.error("请求参数不正确:"+name+":"+item+","+ex.getMessage());
        return "";
    }
}
	
```



## 自定义参数

将此文件上传到/etc/zabbix/zabbix_agentd.d目录下,并重启agent

userparameter_responsetimel.conf

```properties
UserParameter=getItems[*],curl 'http://$1:$2/app/monitor/zabbix?host=$1&port=$2' 2>/dev/null
UserParameter=getItemValue[*],curl 'http://$1:$2$3?name=$4&item=$5' 2>/dev/null
```

### 自动发现

```properties
#直接访问
curl http://192.168.1.103:8080/app/monitor/zabbix
#配置
UserParameter=getItems[*],curl 'http://$1:$2/app/monitor/zabbix?host=$1&port=$2' 2>/dev/null
#验证
zabbix_get -s 192.168.1.105 -p 10050 -k "getItems[192.168.1.103,8080,time]"
```

返回内容

```json
{
  "data": [
    {
      "{#ITEM0}": "5",
      "{#ITEM1}": "10",
      "{#ITEM2}": "100",
      "{#ITEM3}": "500",
      "{#ITEM4}": "1000",
      "{#ITEM5}": "5000",
      "{#NAME}": "/app/monitor/zabbix",
      "{#URL}": "/app/monitor/getItem"
    },
    {
      "{#ITEM0}": "5",
      "{#ITEM1}": "10",
      "{#ITEM2}": "100",
      "{#ITEM3}": "500",
      "{#ITEM4}": "1000",
      "{#ITEM5}": "5000",
      "{#NAME}": "httpRequest.totalrequest",
      "{#URL}": "/app/monitor/getItem"
    },
    {
      "{#ITEM0}": "5",
      "{#ITEM1}": "10",
      "{#ITEM2}": "100",
      "{#ITEM3}": "500",
      "{#ITEM4}": "1000",
      "{#ITEM5}": "5000",
      "{#NAME}": "/app/monitor/getItem",
      "{#URL}": "/app/monitor/getItem"
    }
  ],
  "success": true,
  "state": 2
}
```

### 获取某项的值

```properties
#直接访问
curl http://192.168.1.103:8080/app/monitor/getItem?name=/app/monitor/zabbix&item=0
#配置
UserParameter=getItemValue[*],curl 'http://$1:$2$3?name=$4&item=$5' 2>/dev/nul
#验证
zabbix_get -s 192.168.1.105 -p 10050 -k "getItemValue[192.168.1.103,8080,/app/monitor/timeValue,/app/monitor/time,0]"
```

返回内容

```json
3
```

## 配置模板

### 新增模板

```properties
Templates->Create Template

Template name:httpTemplate

Add
```



### 新增发现规则

```properties
Templates->httpTemplate->Discovery rules->Create discovery rule

Name:httpDiscovery

Type:Zabbix agent/Zabbix agent(active)

Key :getItems[{$MONITOR_HOST},{$MONITOR_PORT}]

点击Add
```



### 新增监控项(Item prototypes)

```properties
Templates->httpTemplate->Discovery rules->httpDiscovery->Item prototypes->Create item prototype

Name:{#NAME}_{#ITEM0}

Type:Zabbix agent/Zabbix agent(active)

Key:getItemValue[{$MONITOR_HOST},{$MONITOR_PORT},{#URL},{#NAME},0]

点击Add

```



继续添加item1,2,3,4,5

![1532601959606](/blog/images/1532601959606.png)

### 监控图(Graph prototypes)

Templates->httpTemplate->Graph prototypes->Create graph prototype

```properties
Name:{#NAME}
Graph type:Stacked
```



- Add prototype

![1532602093961](/blog/images/1532602093961.png)

 Add

### 将模板关联到目标主机

```properties
host->要监控的host->Templates->select->	httpTemplate->Select->Add->Update

host->要监控的host->Macros

{$MONITOR_HOST}:192.168.1.103

add

{$MONITOR_PORT}:8080

update
```

![1532602282270](/blog/images/1532602282270.png)



最终效果

![img](/blog/images/clipboard-1533045789586.png) 

# 中文

修改系统语言

![1532643942576](/blog/images/1532643942576.png)

替换字体

1. 到C:\Windows\Fonts下找到需要的字体上传到/usr/share/zabbix/fonts
2. 备份原字体
3. 替换

```shell
#备份
mv /usr/share/zabbix/fonts/graphfont.ttf /usr/share/zabbix/fonts/graphfont.ttf.bak
#替换
ln /usr/share/zabbix/fonts/SIMFANG.TTF /usr/share/zabbix/fonts/graphfont.ttf
```