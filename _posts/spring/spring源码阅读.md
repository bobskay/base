---
title: spring源码阅读
date: 2019-10-11 10:08:29
categories: [源码阅读,spring]
tags: [spring]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

spring源码阅读

<!--more-->

# 下载

```shell
#下载
git clone https://github.com/spring-projects/spring-framework.git
git branch -r
#切换指定分支
git checkout -b 5.0.x origin/5.0.x
```

# 导入

事先要安装好gradle，下载直接解压就好了

<http://services.gradle.org/distributions/>

![1570771628089](/blog/images/1570811876630.png)



# spring启动过程

## 构造方法

org.springframework.context.annotation.AnnotationConfigApplicationContext

```java
public AnnotationConfigApplicationContext(Class<?>... annotatedClasses) {
    this();
    register(annotatedClasses);//读取配置信息
    refresh();//实际创建
}
```

## 创建方法

org.springframework.context.support.AbstractApplicationContext#refresh

```java
public void refresh() throws BeansException, IllegalStateException {
    prepareRefresh();
    ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
    prepareBeanFactory(beanFactory);
    postProcessBeanFactory(beanFactory);
    invokeBeanFactoryPostProcessors(beanFactory);//手动注册bean
    //初始化BeanPostProcessor,优先初始化实现了PriorityOrdered接口的类
    registerBeanPostProcessors(beanFactory);
    initMessageSource();
    initApplicationEventMulticaster();
    onRefresh();
    registerListeners();
    finishBeanFactoryInitialization(beanFactory);//实际创建
    finishRefresh();
    resetCommonCaches();
}

```

### 手动注册bean

invokeBeanFactoryPostProcessors最终会调用

org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions

该类扫描系统配置的bean，根据bean实现的接口添加bean的注册信息，主要有以下接口

1. org.springframework.context.annotation.ImportBeanDefinitionRegistrar.registerBeanDefinitions

通过传入的BeanDefinitionRegistry.registerBeanDefinition添加

```java
 @Override
public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    ......
	registry.registerBeanDefinition("ImportBeanDefinitionRegistrarBook",definition);

}
```

2. org.springframework.context.annotation.ImportSelectorr

通过返回的类名添加

```java
public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    return new String[]{......};
}
```

### 初始化

初始化所有非lazy的单例

AbstractApplicationContext.finishBeanFactoryInitialization-->

DefaultListableBeanFactory.preInstantiateSingletons-->

AbstractBeanFactory.getBean-->

AbstractAutowireCapableBeanFactory.createBean

```java
   /**
     * 最终创建bean的方法，具体调用doCreateBean
     * 1、 resolveBeforeInstantiation(beanName, mbdToUse);
     *        生成代理对象,具体执行InstantiationAwareBeanPostProcessor的postProcessBeforeInstantiation方法
     *        如果返回值不为null,直接返回代理对象
     * 2、 createBeanInstance(beanName, mbd, args); new实例对象
     * 3   applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
     *          调用MergedBeanDefinitionPostProcessor
     * 4、 populateBean(beanName, mbd, instanceWrapper); 填充bean的基础信息
     *      1)InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation 调用处理器
     *      2)InstantiationAwareBeanPostProcessor.postProcessPropertyValues 调用处理器
     *      3)applyPropertyValues(beanName, mbd, bw, pvs);利用反射赋值
     * 5、 initializeBean(beanName, exposedObject, mbd); 执行初始化动作
     *      1) invokeAwareMethods(beanName, bean);   调用aware方法
     *      2) applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName); 调用所有beanprocessor的applyBeanPostProcessorsBeforeInitialization
     *      3) invokeInitMethods(beanName, wrappedBean, mbd); 调用自定义的方法
     *      4) applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName); 调用所有beanprocessor的applyBeanPostProcessorsAfterInitialization
     * 6、 注册销毁方法
     * */
```
#### createBean

```java
//调用InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation，给用户自己创建实例的机会。如果返回结果为null，就调用doCreateBean创建
protected Object createBean(String beanName, RootBeanDefinition mbd,  Object[] args)
            throws BeanCreationException {

    Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
    if (bean != null) {
        return bean;
    }
    Object beanInstance = doCreateBean(beanName, mbdToUse, args);
    return beanInstance;
}
```
#### doCreateBean

```java
/**
1 new实例,调用构造函数
2 调用org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition给用户机会修改beanDefinition
3 自动注入属性值
4 执行用户自定义的初始化方法
**/
```

```java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
			throws BeanCreationException {
    BeanWrapper instanceWrapper = createBeanInstance(beanName, mbd, args);//1
    applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);//2
    populateBean(beanName, mbd, instanceWrapper);//3
    Object exposedObject = initializeBean(beanName, exposedObject, mbd);//4
    return exposedObject;
}
```

#### populateBean

```java

protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
    //修改new出来后的bean值
    for (BeanPostProcessor bp : getBeanPostProcessors()) {
        if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                continueWithPropertyPopulation = false;
                break;
            }
        }
    }
    
    
    //修改要注入的属性
    for (BeanPostProcessor bp : getBeanPostProcessors()) {
        if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
            if (pvs == null) {
                return;
            }
        }
    }
}
```

#### initializeBean

```java
protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    //执行@PostConstruct标注的方法
    //执行org.springframework.beans.factory.InitializingBean#afterPropertiesSet
    Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
    //执行bean注解里的方法@Bean(initMethod = "init",destroyMethod = "destory")
    invokeInitMethods(beanName, wrappedBean, mbd);
    //执行org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization最后一次设置值
    return applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
}
```

## spring启动总结

1. 通过ImportSelectorr和ImportBeanDefinitionRegistrar实现手动添加bean定义
2. 通过InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation手动向容器添加bean
3. 通过InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation修改初始化后的bean属性
4. 单个bean如果要在初始化后要做些特殊操作，可以实现InitializingBean.afterPropertiesSet
5. 通过BeanPostProcessor#postProcessAfterInitialization可以在所有bean初始化完毕后最后一次进行修改，此时甚至可以将整个bean替换掉


   注意： 如果要修改属性的bean是BeanPostProcessor，必须保证你的类在目标bean之前加载，所以要修改BeanPostProcessor值的BeanPostProcessor需要实现PriorityOrdered接口以保证加载顺序

# springboot启动

入口

new SpringApplication对象然后执行run方法

```java
public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
    return new SpringApplication(primarySources).run(args);
}
```

## new SpringApplication

最主要代码是读取META-INF/spring.factories文件里的配置信息然后保存起来


核心部分是读取SpringFactoriesInstances和Listeners

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		....
        //配置SpringFactoriesInstances
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
    	//配置Listeners
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();
	}
```

## run

org.springframework.boot.SpringApplication#run(java.lang.String...)

```java
public ConfigurableApplicationContext run(String... args) {
   //new刚才获取listeners并且调用每个listener的starting
   SpringApplicationRunListeners listeners = getRunListeners(args);
   listeners.starting();
   ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
   //准备环境并且调用listeners.environmentPrepared(environment);
   ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
    
   ConfigurableApplicationContext context=createApplicationContext();
   //准备上下文执行所有initializer.initialize(context),以及监听器的contextPrepared和contextLoaded
   prepareContext(context, environment, listeners, applicationArguments, printedBanner); 
   ///刷新容器,执行的就是spring的创建就容器方法
   refreshContext(context);
   afterRefresh(context, applicationArguments);
}
```

## prepareContext

```java
private void prepareContext(ConfigurableApplicationContext context, ConfigurableEnvironment environment,
                            SpringApplicationRunListeners listeners, ApplicationArguments applicationArguments, Banner printedBanner) {
    context.setEnvironment(environment);
    postProcessApplicationContext(context);
    applyInitializers(context);
    listeners.contextPrepared(context);
    load(context, sources.toArray(new Object[0]));
    listeners.contextLoaded(context);
}
```

# 事务

## 最终执行方法

org.springframework.transaction.interceptor.TransactionAspectSupport#invokeWithinTransaction

```java
protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
			final InvocationCallback invocation) throws Throwable {
    ......
  	//创建事务AbstractPlatformTransactionManager.getTransaction
	TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
    Object retVal;
    try {
        retVal = invocation.proceedWithInvocation();//执行业务方法
    }
    catch (Throwable ex) {
        completeTransactionAfterThrowing(txInfo, ex);//出错处理
        throw ex;
    }
    finally {
        cleanupTransactionInfo(txInfo);//恢复事务状态到方法执行前
    }
    commitTransactionAfterReturning(txInfo);//正常完成
    return retVal;
}
```

## 创建事务

org.springframework.transaction.support.AbstractPlatformTransactionManager#getTransaction

```java
/**
 * 果存在事务,根据事务的传播机制进行处理
 * REQUIRED, 如果当前线程已经在一个事务中，则加入该事务，否则新建一个事务。
 * SUPPORT, 如果当前线程已经在一个事务中，则加入该事务，否则不使用事务。
 * MANDATORY(强制的)，如果当前线程已经在一个事务中，则加入该事务，否则抛出异常。
 * REQUIRES_NEW，无论如何都会创建一个新的事务，如果当前线程已经在一个事务中，则挂起当前事务，创建一个新的事务。
 * NOT_SUPPORTED，如果当前线程在一个事务中，则挂起事务。
 * NEVER，如果当前线程在一个事务中则抛出异常。
 * NESTED, 执行一个嵌套事务，有点像REQUIRED，但是有些区别，在Mysql中是采用SAVEPOINT来实现的。
 */
```

```java
public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
    ......
    //如果已经存在就根据传播机制进行处理
    if (isExistingTransaction(transaction)) {
        return handleExistingTransaction(definition, transaction, debugEnabled);
    }
    //创建事务对象
    DefaultTransactionStatus status = newTransactionStatus(
						definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
    //调用jdbc代码DataSourceTransactionManager.doBegin
	doBegin(transaction, definition);
}
```

## 开启事务

org.springframework.jdbc.datasource.DataSourceTransactionManager#doBegin

```java
protected void doBegin(Object transaction, TransactionDefinition definition) {
    DataSourceTransactionManager.DataSourceTransactionObject txObject = (DataSourceTransactionManager.DataSourceTransactionObject)transaction;
    txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
    Connection con = txObject.getConnectionHolder().getConnection();
    //设为手动提交事务
    con.setAutoCommit(false);
    设置超时
    int timeout = determineTimeout(definition);
    if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
        txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
    }
    //将connection绑定到当前线程
	TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
}
```

## 修改回滚规则

默认配置下只有RuntimeException和Error的异常才回滚，可以通过修改默认配置实现特定异常也回滚

### 原理

系统配置的EnableTransactionManagement注解会导入TransactionManagementConfigurationSelector

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TransactionManagementConfigurationSelector.class)
public @interface EnableTransactionManagement {
    ......
}
```

org.springframework.transaction.annotation.TransactionManagementConfigurationSelector#selectImports

默认是adviceMode=PROXY，所以启动的时候会加载ProxyTransactionManagementConfiguration

```java
protected String[] selectImports(AdviceMode adviceMode) {
    switch (adviceMode) {
        case PROXY:
            return new String[] {AutoProxyRegistrar.class.getName(),
                                 ProxyTransactionManagementConfiguration.class.getName()};
        case ASPECTJ:
            return new String[] {determineTransactionAspectClass()};
        default:
            return null;
    }
}
```

ProxyTransactionManagementConfigurations是一个配置类，配置了TransactionInterceptor

```java
@Configuration
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {
    @Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionInterceptor transactionInterceptor() {
        ......
}
```

系统最终会从TransactionInterceptor里拿到TransactionAttributeSource

调用TransactionAttributeSource传入method和class获得TransactionAttribute

根据TransactionAttribute的rollbackOn返回值判断是否回滚

```java
TransactionInterceptor.getTransactionAttributeSource().getTransactionAttribute(method,targetClass).rollbackOn(exception)
```

### 最终代码

```java
/**
 * TransactionInterceptor的后置处理器
 * 修改默认的TransactionAttributeSource以实现对Exception异常的回滚
 * */
@Slf4j
public class TransactionInterceptorPostProcessor implements BeanPostProcessor, PriorityOrdered {
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof TransactionAspectSupport)) {
            return bean;
        }
        TransactionInterceptor interceptor = (TransactionInterceptor) bean;
        TransactionAttributeSource source = interceptor.getTransactionAttributeSource();
        interceptor.setTransactionAttributeSources(new MyTransactionAttributeSource(source));
        log.debug("将TransactionInterceptor的TransactionAttributeSource修改为MyTransactionAttributeSource");


        return interceptor;
    }

    @Override
    //提高优先级保证在TransactionInterceptor前执行
    public int getOrder() {
        return 0;
    }

    public class MyTransactionAttributeSource implements TransactionAttributeSource {
        private TransactionAttributeSource target;
        public MyTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
            this.target = transactionAttributeSource;
        }
        @Override
        public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
            TransactionAttribute attribute = target.getTransactionAttribute(method, targetClass);
            if(attribute instanceof RuleBasedTransactionAttribute){
                RuleBasedTransactionAttribute rule=(RuleBasedTransactionAttribute)attribute;
                //添加规则让Exception异常也回滚
                rule.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
            }
            return attribute;
        }
    }
}

```













