---
title: dubbo源码
date: 2019-10-25 23:51:07
categories: [源码]
tags: [dubbo]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

dubbo源码

<!--more-->

解析标签

com.alibaba.dubbo.config.spring.schema.DubboNamespaceHandler

com.alibaba.dubbo.config.spring.schema.DubboBeanDefinitionParser

com.alibaba.dubbo.config.spring.ServiceBean

com.alibaba.dubbo.config.ServiceConfig

com.alibaba.dubbo.rpc.Invoker

com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol

```sequence
NSHandler->NSHandler: init()
NSHandler->DDParser: 
DDParser->DDParser: parse
DDParser->ServiceBean: 
ServiceBean->ServiceBean: afterPropertiesSet
ServiceBean->ServiceBean: export
ServiceBean->ServiceConfig: doExport
ServiceConfig->ServiceConfig: doExportUrls
ServiceConfig->ServiceConfig: doExportUrlsFor1Protocol
ServiceConfig->ProxyFactory: getInvoker
ServiceConfig->DubboProtocol: export
DubboProtocol->DubboProtocol: openServer
DubboProtocol->DubboProtocol: createServer

```

初始化dubbo协议，创建netty服务器

com.alibaba.dubbo.remoting.exchange.Exchangers

com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchanger

com.alibaba.dubbo.remoting.Transporters

com.alibaba.dubbo.remoting.transport.netty.NettyTransporter

com.alibaba.dubbo.remoting.transport.netty.NettyServer

```sequence
DubboProtocol->DubboProtocol: createServer
DubboProtocol->Exchangers: bind
Exchangers->HeaderExchanger: bind
HeaderExchanger->Transporters: bind
Transporters->NettyTransporter: bind
NettyTransporter->NettyServer: new
```

初始化注册中心，创建Zookeeper客户端，并创建节点

com.alibaba.dubbo.registry.integration.RegistryProtocol

com.alibaba.dubbo.registry.Registry

com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistryFactory

com.alibaba.dubbo.registry.support.FailbackRegistry

com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry#doRegister

com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient#create

```sequence
RProtocol->RProtocol:register
RProtocol->ZkFactory:getRegistry
Note right of ZkFactory: 获取zk客户端
ZkFactory-->RProtocol:createRegistry
RProtocol->FbRegistry:register
FbRegistry->ZkRegistry:doRegister
ZkRegistry->ZkClient:create
Note right of ZkClient: 创建zk节点
```

服务引用

ReferenceBean

com.alibaba.dubbo.registry.integration.RegistryProtocol#refer

com.alibaba.dubbo.rpc.cluster.Cluster#join

com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol#refer

com.alibaba.dubbo.rpc.ProxyFactory#getProxy

```sequence
RefBean->RefBean:get
RefBean->RefBean:init
RefBean->RefConfig:createProxy
RefConfig->ReProtoco:refer
ReProtoco->ReProtoco:doRefer
ReProtoco->Cluster:join
Cluster->DubboProtocol:refer
DubboProtocol->DubboProtocol:getClients
DubboProtocol->DubboProtocol:initClient
DubboProtocol->NettyClient:new 
DubboProtocol-->RefConfig:返回DubboInvoker
RefConfig->ProxyFactory:getProxy
ProxyFactory-->RefBean:生成Bean放入容器
```

调用 

com.alibaba.dubbo.rpc.proxy.InvokerInvocationHandler

com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker#invoke

com.alibaba.dubbo.rpc.cluster.support.FailoverClusterInvoker#doInvoke

com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker#doInvoke
com.alibaba.dubbo.remoting.exchange.ExchangeChannel#request(java.lang.Object, int)




```sequence
InvokerHandler->Invoker:invoke
Invoker->AbsClsrInvoker:invoke
AbsClsrInvoker->FailInvoker:doInvoke
FailInvoker->DubboInvoke:doInvoke
DubboInvoke->ExchangeChannel:request
ExchangeChannel->ExchangeChannel:get
ExchangeChannel-->InvokerHandler:Result
InvokerHandler->Result:create
Result-->InvokerHandler:最终结果

```



