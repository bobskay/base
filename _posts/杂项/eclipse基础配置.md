---
title: eclipse初始化
date: 2018-07-27 17:27:11
categories: [开发工具,eclipse]
tags: [eclipse]
typora-root-url: E:/blog
---
Eclipse安装完后，根据个人的一些习惯，有些默认配置需要做一些更改，包括Jdk、错误提示、maven、系统编码等等

<!--more-->
# 将安装的jre路径改为jdk

位置window->preferences->jre

![1531789885950](/blog/images/1531789885950.png)



![1531789666976](/blog/images/1531789666976.png)

选择jdk所在目录

![1531789795903](/blog/images/1531789795903.png)

# 修改错误提示

window->preferences->err

![1531790122998](/blog/images/15317900641398.png)

## unused import

![1531790270731](/blog/images/1531790270731.png)

## serialVersionUID

![1531790323339](/blog/images/1531790323339.png)

## generic types

![1531790431180](/blog/images/1531790431180.png)

# Maven配置

## 创建setting文件

设置本地库路径 localRepository和镜像仓库

```xml
<?xml version="1.0" encoding="UTF-8"?>

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<!--指定本地仓库存储路径。默认值为~/.m2/repository 即 ${user.home}/.m2/repository。 -->
	<localRepository>E:/opt/config/maven/.m2/repository</localRepository>

	<!-- 指定镜像列表，用于从远程仓库下载资源,如果你想覆盖中央仓库的默认地址，那么这里我们就会使用的镜像了 -->
	<mirrors>
		<!-- 指定仓库的镜像站点，代替一个给定的库。该镜像藏库有一个ID相匹配的mirrorOf元素。 ID是用于继承和直接查找目的，必须是唯一的。 -->
		<mirror>
			<!--该镜像的唯一标识符。id用来区分不同的mirror元素。 -->
			<id>mirrorId</id>
			<!--被镜像的服务器的id，比如：central，不能和id匹配。 -->
			<mirrorOf>central</mirrorOf>		
			<name>aliyun mirror</name
			<!--镜像地址-->
			<url>http://maven.aliyun.com/nexus/content/groups/public</url>
		</mirror>
	</mirrors>
        
    <!--将jdk版本设置为1.8-->
     <profile>
		<id>jdk-1.8</id>
		<activation>
			<activeByDefault>true</activeByDefault>
			<jdk>1.8</jdk>
		</activation>
		<properties>
			<maven.compiler.source>1.8</maven.compiler.source>
			<maven.compiler.target>1.8</maven.compiler.target>
			<maven.compiler.compilerVersion>1.8</maven.compiler.compilerVersion>
		</properties>
	</profile>
</settings>
```



## 配置文件路径

![1531790966563](/blog/images/1531790543101.png)

设置后

![1531793557326](/blog/images/1531793557326.png)

# 常用模板

位置

window->preferences->java templates

![1531791366388](/blog/images/1531791366388.png)

## logger

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class xxx {
	public static final Logger log = LoggerFactory.getLogger(xxx.class);
```



设置

![1531793308559](/blog/images/1531793308559.png)

Name 快捷键

Pattern 模板内容

```java
${:import(org.slf4j.Logger,org.slf4j.LoggerFactory)}public static final Logger log = LoggerFactory.getLogger(${enclosing_type}.class);
```

注意:

​	用public是因为当log没被用到的时候不出现warring提示

​	import和代码如果不在同一行,生成的代码会多一个回车

# 修改默认编码为UTF-8

![1532132971603](/blog/images/1532132971603.png)

# lockmoc

## 安装

将 lombok.jar 放在eclipse安装目录下，和 eclipse.ini 文件平级的 

`java -jar lombok.jar`  

## 常用标签

- @Setter
- @Getter
- @Data=getter、setter、equals、canEqual、hashCode、toString 
- @Log(这是一个泛型注解，具体有很多种形式)
- @AllArgsConstructor
- @NoArgsConstructor
- @EqualsAndHashCode
- @NonNull
- @Cleanup `@Cleanup`清理的方法为`close` 
- @ToString
- @RequiredArgsConstructor
- @Value
- @SneakyThrows
- @Synchronized