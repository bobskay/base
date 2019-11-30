---
title: redis配置
date: 2019-01-26 13:50:08
categories: [redis]
tags: [redis]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

创建redis集群环境，编码测试

<!--more-->



# 规划

共6个应用

1个master,2个slave,3个sentinel

master : 192.168.1.110 6379

slave1: 192.168.1.110 6380

slave2: 192.168.1.110 6381

sentinel1: 192.168.1.110 26379

sentinel2: 192.168.1.110 26380

sentinel3: 192.168.1.110 26381

# 目录

redis目录

/usr/local/bin/

配置文件目录

/media/sf_linux/soft/redis

该目录下新建6个conf文件

```shell
[root@centos7 redis]# pwd
/media/sf_linux/soft/redis
[root@centos7 redis]# ll
total 89
drwxrwx---. 1 root vboxsf  4096 Jan 31 14:15 logs
-rwxrwx---. 1 root vboxsf   327 Jan 31 14:14 redis6379.conf
-rwxrwx---. 1 root vboxsf   177 Jan 31 14:47 redis6380.conf
-rwxrwx---. 1 root vboxsf   203 Jan 31 14:47 redis6381.conf
-rwxrwx---. 1 root vboxsf 62155 Nov 22 18:26 redis.conf.bak
-rwxrwx---. 1 root vboxsf   803 Jan 31 14:18 sentinel26379.conf
-rwxrwx---. 1 root vboxsf   708 Jan 31 14:47 sentinel26380.conf
-rwxrwx---. 1 root vboxsf   708 Jan 31 14:47 sentinel26381.conf
```

日志目录

/media/sf_linux/soft/redis/logs

数据目录

/opt/redis/data/

临时目录

/opt/redis/data/

创建目录

```shell
mkdir /opt/redis/data/
mkdir /opt/redis/data/temp
mkdir /media/sf_linux/soft/redis/logs
mkdir /media/sf_linux/soft/redis
```

# 配置

## master

redis6379.conf

```properties
#访问ip
bind 192.168.1.110
#工作目录
#不知道什么原因如果目录配置在/media/sf_linux下,sentinel无法生成文件,所以换个目录,可能是因为/media/sf_linux是windows的映射目录
dir "/opt/redis/data"
#dump文件名
dbfilename "dump6379.rdb"
#端口
port 6379
#日志文件
logfile "/media/sf_linux/soft/redis/logs/6379.log"
#后台方式启动
daemonize yes
```

## slave1

redis6380.conf

```properties
bind 192.168.1.110
dir "/opt/redis/data"
daemonize yes
#dump文件名,端口,日志文件改为6380
dbfilename "dump6380.rdb"
port 6380
logfile "/media/sf_linux/soft/redis/logs/6380.log"
slaveof 192.168.1.110 6379

```

## slave2

redis6381.conf

```properties
bind 192.168.1.110
dir "/opt/redis/data"
daemonize yes
#dump文件名,端口,日志文件改为6381
dbfilename "dump6381.rdb"
port 6381
logfile "/media/sf_linux/soft/redis/logs/6381.log"
slaveof 192.168.1.110 6379
```

## sentinel1

sentinel26379.conf

```properties
bind 192.168.1.110
port 26379
daemonize yes
logfile "/media/sf_linux/soft/redis/logs/sentia26379.log"
dir "/opt/redis/data/temp"
#master名称叫mymaster,java应用里需要用到这个名字
#1只要有1个sentinel认为服务器挂了就切换
sentinel monitor mymaster 192.168.1.110 6379 1
#10000,服务器挂掉10秒后切换
sentinel down-after-milliseconds mymaster 10000
```

## sentinel2

sentinel26380.conf

```properties
bind 192.168.1.110
daemonize yes
dir "/opt/redis/data/temp"
sentinel monitor mymaster 192.168.1.110 6379 1
sentinel down-after-milliseconds mymaster 10000
#端口和日志改为26380
port 26380
logfile "/media/sf_linux/soft/redis/logs/sential26380.log"
```

## sentinel3

sentinel26381.conf

```properties
bind 192.168.1.110
daemonize yes
dir "/opt/redis/data/temp"
sentinel monitor mymaster 192.168.1.110 6379 1
sentinel down-after-milliseconds mymaster 10000
#端口和日志改为26381
port 26381
logfile "/media/sf_linux/soft/redis/logs/sential26381.log"
```

# 启动

## redis

```shell
/usr/local/bin/redis-server /media/sf_linux/soft/redis/redis6379.conf 
/usr/local/bin/redis-server /media/sf_linux/soft/redis/redis6380.conf 
/usr/local/bin/redis-server /media/sf_linux/soft/redis/redis6381.conf 
```

## sentinel

```shell
/usr/local/bin/redis-sentinel /media/sf_linux/soft/redis/sentinel26379.conf 
/usr/local/bin/redis-sentinel /media/sf_linux/soft/redis/sentinel26380.conf 
/usr/local/bin/redis-sentinel /media/sf_linux/soft/redis/sentinel26381.conf 
```

## 查看

ps -ef|grep redis

```shell
[root@centos7 ~]# ps -ef|grep redis
root     13041     1  0 14:14 ?        00:00:00 /usr/local/bin/redis-server 192.168.1.110:6379
root     13054     1  0 14:14 ?        00:00:00 /usr/local/bin/redis-server 192.168.1.110:6380
root     13063     1  0 14:14 ?        00:00:00 /usr/local/bin/redis-server 192.168.1.110:6381
root     13270     1  0 14:17 ?        00:00:00 /usr/local/bin/redis-sentinel 192.168.1.110:26379 [sentinel]
root     13278     1  0 14:18 ?        00:00:00 /usr/local/bin/redis-sentinel 192.168.1.110:26380 [sentinel]
root     13285     1  0 14:18 ?        00:00:00 /usr/local/bin/redis-sentinel 192.168.1.110:26381 [sentinel]
root     13350 11045  0 14:19 pts/0    00:00:00 grep --color=auto redis
```

# java

## 所需jar包

```xml
 <dependency>
     <groupId>redis.clients</groupId>
     <artifactId>jedis</artifactId>
     <version>2.7.2</version>
</dependency>
```

## 测试redis

```java
package xxx.xxx;

import redis.clients.jedis.Jedis;

public class RedisTest {
    public static void main(String args[]){
        System.out.println("--------master-----------");
        Jedis master=new Jedis("192.168.1.110",6379);
        master.set("hello","redis");
        String message=master.get("hello");
        System.out.println("hello "+message);

        System.out.println("--------slave-----------");
        Jedis slave1=new Jedis("192.168.1.110",6380);
        slave1.set("hello","redis");//这里报错
        message=slave1.get("hello");
        System.out.println("hello "+message);
    }
}
```

运行结果

```log
--------master-----------
hello redis
--------slave-----------
Exception in thread "main" redis.clients.jedis.exceptions.JedisDataException: READONLY You can't write against a read only slave.
	at redis.clients.jedis.Protocol.processError(Protocol.java:117)
	at redis.clients.jedis.Protocol.process(Protocol.java:142)
	at redis.clients.jedis.Protocol.read(Protocol.java:196)
	at redis.clients.jedis.Connection.readProtocolWithCheckingBroken(Connection.java:288)
	at redis.clients.jedis.Connection.getStatusCodeReply(Connection.java:187)
	at redis.clients.jedis.Jedis.set(Jedis.java:66)
	at wang.wangby.redis.RedisTest.main(RedisTest.java:14)
```

## 测试集群

### 正常访问

每1秒访问一次redis集群，get and set 1个变量（no）的值，然后将这个值+1

```java
package wang.wangby.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

public class SentinelsTest {

    public static void main(String args[]) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);//最大连接数
        config.setMaxWaitMillis(60000);// 设置最大阻塞时间，
        config.setMaxIdle(10);// 设置空间连接
        Set<String> sentinels = new HashSet<String>();
        sentinels.add("192.168.1.110:26379");
        sentinels.add("192.168.1.110:26380");
        sentinels.add("192.168.1.110:26381");
        JedisSentinelPool pool = new JedisSentinelPool("mymaster", sentinels, config);

        int no=0;
        while (true) {
            try {
                Jedis jedis = pool.getResource();
                no++;
                jedis.set("count", no+"");
                jedis.close();
                System.out.println("count: "+jedis.get("count"));
                Thread.sleep(1000);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
```

输出

```log
count: 1
count: 2
count: 3
count: 4
...
```

### 模拟sentinel挂掉

kill

```shell
[root@centos7 ~]# lsof -i:26379|awk 'NR==2{print $2}' 
13270
[root@centos7 ~]# kill 13270
```

#### 控制台

报出警告，但数据仍能正常访问

```verilog
count: 338
count: 339
count: 340
count: 341
一月 31, 2019 2:44:59 下午 redis.clients.jedis.JedisSentinelPool$MasterListener run
严重: Lost connection to Sentinel at 192.168.1.110:26379. Sleeping 5000ms and retrying.
count: 342
count: 343
count: 344
```

### 模拟master挂掉

kill

```shell
[root@centos7 ~]# lsof -i:6379|awk 'NR==2{print $2}' 
13041
[root@centos7 ~]# kill 13041
[root@centos7 ~]# 
```

#### 控制台

有10秒钟无法访问redis(Could not get a resource from the pool)，10秒后恢复正常

```verilog
count: 482
count: 483
Unexpected end of stream.
Could not get a resource from the pool
一月 31, 2019 2:47:24 下午 redis.clients.jedis.JedisSentinelPool$MasterListener run
严重: Lost connection to Sentinel at 192.168.1.110:26379. Sleeping 5000ms and retrying.
Could not get a resource from the pool
Could not get a resource from the pool
Could not get a resource from the pool
Could not get a resource from the pool
Could not get a resource from the pool
Could not get a resource from the pool
一月 31, 2019 2:47:30 下午 redis.clients.jedis.JedisSentinelPool$MasterListener run
严重: Lost connection to Sentinel at 192.168.1.110:26379. Sleeping 5000ms and retrying.
Could not get a resource from the pool
Could not get a resource from the pool
Could not get a resource from the pool
一月 31, 2019 2:47:33 下午 redis.clients.jedis.JedisSentinelPool initPool
信息: Created JedisPool to master at 192.168.1.110:6380
Could not get a resource from the pool
count: 485
count: 486
count: 487
一月 31, 2019 2:47:36 下午 redis.clients.jedis.JedisSentinelPool$MasterListener run
严重: Lost connection to Sentinel at 192.168.1.110:26379. Sleeping 5000ms and retrying.
count: 488
count: 489
```

#### 配置文件

redis6381.conf的最后一行slaveof从6379变更为6380

```properties
slaveof 192.168.1.110 6380
```

redis6381.conf文件后面的slaveof被删掉了

所有sentinel的monitor自动变更为6380

```properties
sentinel monitor mymaster 192.168.1.110 6380 1
```

重启6379后

redis6381.conf文件末尾自动加上

```properties
# Generated by CONFIG REWRITE
slaveof 192.168.1.110 6380
```

# 监控

## 日志

jedis 使用java.util.logging，日志格式和现有监控系统不一样，需要手动修改一下

日志级别 java.util.logging.Level

| SEVERE | WARNING | INFO | CONFIG | FINE | FINER | FINEST |
| ------ | ------- | ---- | ------ | ---- | ----- | ------ |
| 严重   | 警告    | 信息 | ------ | ---- | ----- | ------ |
在根目录创建logging.propertie

```properties
handlers= java.util.logging.ConsoleHandler
java.util.logging.ConsoleHandler.level = INFO
java.util.logging.ConsoleHandler.formatter =wang.wangby.redis.MyFormat
```

程序启动的时候加载

```java
java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
InputStream input=LogTest.class.getResourceAsStream("/logging.properties");
logManager.readConfiguration(input);
```

自定义format类（继承java.util.logging.Formatter实现format方法）

```java
 // @see java.util.logging.SimpleFormatter#format;
public class MyFormat extends Formatter {
    @Override
    public String format(LogRecord record) {
        String source;
        if (record.getSourceClassName() != null) {
            String fullName = record.getSourceClassName();
            int point = fullName.lastIndexOf(".");
            if (point != -1) {
                source = fullName.substring(point + 1);
            } else {
                source = fullName;
            }
            if (record.getSourceMethodName() != null) {
                source += "." + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.println();
            record.getThrown().printStackTrace(pw);
            pw.close();
            throwable = sw.toString();
        }
       
        String data = DateTime.current(DateTime.YEAR_TO_MILLISECOND).toString();
        return data + " " + record.getLevel() + " " + source + " " + message + " " + throwable + "\n";

    }
}
```



<!--more-->