---
title: lazydoc使用手册
date: 2018-07-30 18:01:30
categories: [manual]
tags: [lazydoc]
typora-copy-images-to: ../../images
typora-root-url:  E:/blog
---

lazydoc，一个简单的接口文档生成器。

功能，自动生成接口文档，并为每个接口提供一个测试页面。

实现原理，扫描所有带有@controller注解的类，解析带有@RequestMapping的方法，根据方法签名和自定义标签生成文档

最终生成的效果：[点这里](/blog/demo/lazydoc/main.html)  网站只是静态页面，所以大部分按钮是无效的

<!--more-->

下面就以一个简单的spring-boot工程说一下怎么使用

# 运行环境

暂时用到的依赖包

jdk：1.8 

spring-boot-starter-web：2.0.3.RELEASE

velocity：1.7

lombok：1.16.22

目前版本算是初稿，因为用到了一些jdk的新功能，所以jdk1.8是必须。

spring-boot只用到了它的Annotation和AnnotationUtils，所以版本没啥要求，引入时scope我用的是provided

lombok版本是随spring-boot走的

velocity，生成页面的，用习惯了，后续可能会换

# 创建工程

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.example</groupId>
	<artifactId>hello</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>jar</packaging>

	<name>demo</name>
	<description>Demo project for Spring Boot</description>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.0.3.RELEASE</version>
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

## 启动类

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import wang.wangby.lazydoc.controller.LazyDocController;

@SpringBootApplication
public class HelloApplication  {

	public static void main(String[] args) {
		SpringApplication.run(HelloApplication.class, args);
	}
}
```

## 一个简单的Controller

```java
package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "hello world";
	}
}
```

## 运行

http://127.0.0.1:8080/hello

![1533135896268](/blog/images/1533135896268.png)

# hello world

## 引入jar包

```xml
<dependency>
    <groupId>wang.wangby</groupId>
    <artifactId>lazydoc</artifactId>
    <version>0.1</version>
</dependency>
```

## 重启

这时候会看到启动日志里面多了三个地址

```properties
RequestMappingHandlerMapping - Mapped "{[/lazydoc/index]}
RequestMappingHandlerMapping - Mapped "{[/lazydoc/detail/{index}],methods=[GET]}"
RequestMappingHandlerMapping - Mapped "{[/lazydoc/treeView],methods=[GET || POST],produces=[application/json;charset=UTF-8]}
```

## 访问

打开这个页面，就可以看到生成的文档了：http://127.0.0.1:8080/lazydoc/index

![1533136547225](/blog/images/1533136547225.png)

点击测试，可以看到返回内容和网页上的一样

![1533136578789](/blog/images/1533136578789.png)

# 0配置

上面的hello方法，没有参数，返回值也是简单的字符串，所以看不出什么。为了演示，我这里简单的模拟了一个查询的场景。

客户端传过来一些查询条件和分页信息，服务端返回封装好的一页查询结果。

## model类

```java
//图书信息
@Data
public class BookInfo {
	//主键
	private Long bookId;	
	//书名
	private String bookName;
	//出版日期
	private Date publication;
	//标准图书编号
	private String isbn;
	//创建时间
	private Date createTime;
	//售价
	private Integer price;
}
```

## 封装查询条件的对象

```java
//图书查询条件
@Data
public class BookSelector {
	//图书信息
	private BookInfo bookInfo;
	//最低价格
	private Integer minPrice;
	//最高价
	private Integer maxPrice;
}

```

## 封装返回结果的对象

```java
@Data
public class Page<T> {
	//返回的记录集
	private List<T> result;
	//总条数
	private Integer total;
	//查询起始位置
	Integer offset;
	//返回条数
	Integer limit;
}
```

## Controller

这里模拟返回10条

```java
@Controller
@RequestMapping("/book")
//图书管理
public class BookController {

	/**
	 * 查询图书信息
	 * @param selector 查询条件
	 * @param offset 查询
	 * @param limit 返回条数
	 * @return 查询到的结果集
	 * */
	@RequestMapping("/select")
	@ResponseBody
	public Page<BookInfo> select(BookSelector selector, Integer offset, Integer limit) {
		Page page = new Page();
		List list=new ArrayList();
		for(int i=0;i<10;i++) {
			BookInfo inf=new BookInfo();
			inf.setCreateTime(new Date());
			inf.setBookId(i+0L);
			inf.setBookName("bookName:"+i);
			inf.setIsbn("isbn"+i);
			list.add(inf);
		}
		page.setResult(list);
		page.setOffset(0);
		page.setLimit(10);
		page.setTotal(1000);
		return page;
	}
}
```

## 启动并访问

在没有任何配置的情况下，文档也生成了，查看网页版：[点这里](/blog/demo/lazydoc/zero.html)

![1533138903288](/blog/images/1533138903288.png)

# 完善文档

可以看到，按正常方式写代码，没有做任何配置的情况下，接口文档已经生成了。

下面来看看如何完善文档，其实完善就是把说明字段写清楚就好了，lazydoc的所有配置信息均采用注解方式

## 参数和返回值对象

只需要将普通的备注换成@remark便签即可，像这样

```java
@Remark("图书信息")
@Data
public class BookInfo {
	@Remark("主键")
	private Long bookId;
	@Remark("书名")
	private String bookName;
	@Remark("出版日期")
	private Date publication;
	@Remark("标准图书编号")
	private String isbn;
	@Remark("创建时间")
	private Date createTime;
	@Remark("售价")
	private Integer price;
}
```

## controller方法

将原来的注释换个形式

```java
@Remark("查询图书信息")
@Param("查询条件")
@Param("查询起始位置偏移量")
@Param("返回条数")
@Return("查询结果")
@RequestMapping("/select")
@ResponseBody
public Page<BookInfo> select(BookSelector selector, Integer offset, Integer limit) {
    Page page = new Page();
    List list = new ArrayList();
    for (int i = 0; i < 10; i++) {
        BookInfo inf = new BookInfo();
        inf.setCreateTime(new Date());
        inf.setBookId(i + 0L);
        inf.setBookName("bookName:" + i);
        inf.setIsbn("isbn" + i);
        inf.setPrice(i * 100 + i);
        list.add(inf);
    }
    page.setResult(list);
    page.setOffset(0);
    page.setLimit(10);
    page.setTotal(1000);
    return page;
}
```

可以看到，相比于原来的，代码量并没有增多

![1533140024349](/blog/images/1533140055012.png)

## 再次启动

可以看到备注信息已经显示在页面了，网页版：[点这里](/blog/demo/lazydoc/remark.html)

![1533140767437](/blog/images/1533140767437.png)

# 接口测试

## 设置日期格式

为了处理日期，需要给spring添加一个Converter

```java
@Component
public class StringToDateConverter implements Converter<String, Date> {
	private static final String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private static final String shortDateFormat = "yyyy-MM-dd";

	@Override
	public Date convert(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		value = value.trim();
		try {
			return convertInternal(value);
		} catch (Exception ex) {
			throw new RuntimeException("将字符串转为日期失败:" + value + "," + ex.getMessage());
		}
	}

	private Date convertInternal(String value) throws ParseException {
		if (value.matches("^\\d+$")) {
			return new Date(Long.parseLong(value));
		}
		if (value.contains(":")) {
			return new SimpleDateFormat(dateFormat).parse(value);
		} else {
			return new SimpleDateFormat(shortDateFormat).parse(value);
		}
	}
}
```

修改application.properties

```properties
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
spring.jackson.time-zone=GMT+8
```

## 测试接收参数

将select方法稍微改下，将接受到的参数原封传回页面

```java
Page page = new Page();
List list = new ArrayList();
for (int i = 0; i < limit; i++) {
    BookInfo inf = selector.getBookInfo();
    inf.setPrice(selector.getMaxPrice());
    list.add(inf);
}
page.setResult(list);
page.setOffset(offset);
page.setLimit(limit);
page.setTotal(1000);
return page;
```

## 启动并访问

网页版：[点这里](/blog/demo/lazydoc/test.html)

![1533142230223](/blog/images/1533142230223.png)

# 参数配置

## 忽略参数

有事为了省事，把model对象直接放到查询对象里了，但有些查询条件是没用的，可以设置ignores，这样页面就不会显示了

```java
@Param(value="查询条件",ignores= {"bookInfo.bookId","bookInfo.createTime","bookInfo.price","bookInfo.publication"})
```

## 参数校验

### 默认配置

根据数据对应的java类型，默认已经有参数校验了，比如日期和数字。

date类型默认是按YYYY-MM-DD hh:mm:ss校验的，如果只有日期没有时间，在字段上做一些配置

@Property注解，dateOnly参数判断是时间还是日期

```java
@Property(value="出版日期",dateOnly=true)
private Date publication;
```



![1533199895389](/blog/images/1533201045412.png)

### 非空

notNull属性设置为true，提交时就会自动校验了

```java
@Param(value="查询起始位置偏移量",notNull=true)
```

最终效果：[点这里](/blog/demo/lazydoc/main.html)

# json形式的参数

微服务下，接口的参数通常是通过json的方式传递，如果在方法上加了@RequestBody 注解

```java
@Remark("通过json查询图书")
@Param(value="查询条件")
@Param(value="查询起始位置偏移量")
@Param(value="返回条数")
@Return("查询结果")
@RequestMapping("/selectJs")
@ResponseBody
public Page<BookInfo> selectJs(@RequestBody BookSelector selector) {
    Page page = new Page();
    List list = new ArrayList();
    for (int i = 0; i < selector.getLimit(); i++) {
        BookInfo inf = selector.getBookInfo();
        inf.setPrice(selector.getMaxPrice());
        list.add(inf);
    }
    page.setResult(list);
    page.setOffset(selector.getOffset());
    page.setLimit(selector.getLimit());
    page.setTotal(1000);
    return page;
}
```

那么测试页面就会自动变为json方式输入了，同时会把需要的字段自动设置为null，例如这样

![1533203394950](/blog/images/1533203394950.png)

json格式的数据目前还无法做到参数校验，同时只要有1个参数上标注了RequestBody ，页面就会使用json形式

# mock

通常的开发流程是，各部门之间定好接口，然后各自独立开发。当各方进度不一样的时候，为了方便别人测试，需要对方提供一个可供测试的接口。

我的实现方式是，将模拟的返回数据存在缓存中，请求时通过一个唯一标识来获取模拟数据。

暂时做法是将数据放在redis里，为了实现mock功能需要做以下配置

## 引入新的jar包

api-cache：用于将请求内容放入缓存，项目地址https://gitee.com/xixhaha/apicache

spring-boot-starter-aop：通过AOP判断哪些方法需要做mock

spring-boot-starter-data-redis：数据存在redis

```xml
<dependency>
    <groupId>wang.wangby</groupId>
    <artifactId>api-cache</artifactId>
    <version>0.1</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 修改Controller方法注解

这里采用annotation的方式，新增了一个注解Api，这个方内部同时引用了ResponseBody，RequestMapping，Remark

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ResponseBody
@RequestMapping
@Remark
public @interface Api {...
```

所以可以把三个注解去掉，换成下面这样

```java
@Param(value="查询条件",ignores= {"bookInfo.createTime","bookInfo.price","bookInfo.publication","bookInfo.isbn",
                              "minPrice","maxPrice"})
@Return("查询结果")
@Api(name="查询图书信息",path="/select")
public Page<BookInfo> select(BookSelector selector) {
    Page page = new Page();
    List list = new ArrayList();
    for (int i = 0; i < selector.getLimit(); i++) {
        BookInfo b=new BookInfo();
        b.setBookId(i+0L);
        b.setBookName(selector.getBookInfo().getBookName()+i);
        b.setCreateTime(new Date());
        list.add(b);
    }
    page.setResult(list);
    page.setOffset(selector.getOffset());
    page.setLimit(selector.getLimit());
    page.setTotal(1000);
    return page;
}
```

## 配置AOP

接口调用的时候会出现一些异常，同时当多台服务器做集群的时候，由于网络异常需要重试，有可能出现重复请求的情况，出现这些情况如何处理，专门设置了一个类

```java
/** 设置mock异常处理规则 */
public interface ApiMock {

	/**
	 * 当其他服务器正在执行方法的时候,接口调用的返回值
	 * @param method 请求的方法
	 * */ 
	public Object getRunningResult(Method method);

	/**
	 * 当调用方法出现异常后,接口的返回值
	 * 
	 * @param method 请求的方法
	 * @param ex     异常信息
	 */
	public Object getExceptionResult(Method method, Exception ex);

	/**
	 * 获得请求的唯一标识
	 * 
	 * @param method 请求方法
	 * @param args   方法参数
	 */
	public String getRequestId(Method method, Object[] args);
}

```

客户端设置拦截规则的同时，需要实现以上接口，最终的配置类如下

```java
@Configuration
@Slf4j
public class MockConfig {
	
	@Bean
	public MockMethod apiMethods(CacheServer cacheServer) {
		MockMethod method=new  MockMethod();
		method.setCacheServer(cacheServer);
		return method;
	}
	
	@Aspect
	public class MockMethod implements ApiMock{
		CacheServer cacheServer;
		
		public void setCacheServer(CacheServer cacheServer) {
			this.cacheServer=cacheServer;
		}
		
		//只处理有Api注解的方法，同时返回值必须是Page
		@Pointcut("@annotation(wang.wangby.annotation.Api) && execution(com.example.demo.book.model.Page *.*(..))")
		private void apiMethod() {
		}
		

		@Around("apiMethod()")
		public Object process(ProceedingJoinPoint pjp) throws Throwable {
			return new ApiIntercept(pjp,cacheServer).process(this);
		}

		
		@Override
		public Object getRunningResult(Method method) {
			throw new RuntimeException("重复请求");
		}

		//如果请求出现异常,打印异常,并造一个假的Page
		@Override
		public Object getExceptionResult(Method method, Exception ex) {
			log.error(ex.getMessage(),ex);
			Page<BookInfo> page=new Page();
			BookInfo bk=new BookInfo();
			bk.setBookName("查询出错:"+ex.getMessage());
			List list=new ArrayList();
			list.add(bk);
			page.setResult(list);
			return page;
		}

		//通过bookId做为标识
		@Override
		public String getRequestId(Method method, Object[] args) {
			BookSelector sel=(BookSelector) args[0];
			String requestId=sel.getBookInfo().getBookId()+"";
			log.debug("收到请求:"+requestId);
			return requestId;
		}

	}
}
```

## 配置redis

修改application.yaml

```yaml
spring: 
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
```

## 启动程序

bookId设置为123456

![1533303784163](/blog/images/1533303784163.png)

查询一次以后，只要参数里的bookId是123456，其它条件无论写什么返回的都是相同内容，同时redis里面多了一些数据，箭头标注的地方就是请求的唯一标识了，同时可以任意修改里面的内容，这样就可以造出不同的mock数据

![1533303925536](/blog/images/1533303972943.png)