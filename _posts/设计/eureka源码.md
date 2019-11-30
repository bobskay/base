---

title: eureka源码阅读
date: 2019-10-15 15:07:40
categories: [sourcecode]
tags: [springclould，eureka]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

eureka

<!--more-->

# 启动后第一从获取注册信息

系统启动的时候会创建bean：com.netflix.discovery.EurekaClient

通过这个方法创建

```java
//org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.RefreshableEurekaClientConfiguration.eurekaClient
@Bean(destroyMethod = "shutdown")
@ConditionalOnMissingBean(value = EurekaClient.class, search = SearchStrategy.CURRENT)
@org.springframework.cloud.context.config.annotation.RefreshScope
		@Lazy
		public EurekaClient eurekaClient(ApplicationInfoManager manager,
                                         EurekaClientConfig config, EurekaInstanceConfig instance,
                                         @Autowired(required = false) HealthCheckHandler healthCheckHandler) {
    ...
    CloudEurekaClient cloudEurekaClient = new CloudEurekaClient(appManager,
					config, this.optionalArgs, this.context);
    ...
}
```

系统调用http请求用的是httpclient4： CloseableHttpClient#execute

调用链

```flow
st=>operation: EurekaClientAutoConfiguration.RefreshableEurekaClientConfiguration#eurekaClient
a=>operation: CloudEurekaClient#super
b=>operation: DiscoveryClient#<init>
c=>operation: EurekaHttpClientDecorator#getAndStoreFullRegistry
d=>operation: EurekaHttpClientDecorator#getApplications
e=>operation: ApacheHttpClient4Handler#handle
f=>operation: CloseableHttpClient#execute
st->a->b->c->d->e->f
```






