---
title: NIO
date: 2018-07-31 19:05:48
categories: [code,java]
tags: [nio]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

nio

<!--more-->

# Selector 实现原理

linux下select的实现是EpollSelectorImpl

EPollArrayWrapper将Linux的epoll相关系统调用封装成了native方法供EpollSelectorImpl使用。 

```java
private native int epollCreate();
private native void epollCtl(int epfd, int opcode, int fd, int events);
private native int epollWait(long pollAddress, int numfds, long timeout,
                                 int epfd) throws IOException;
```

![img](http://incdn1.b0.upaiyun.com/2017/08/f927dbf90bf79752d25ca2dcc6e5da7e.png) 

## epoll原理

epoll是Linux下的一种IO多路复用技术，可以非常高效的处理数以百万计的socket句柄。
先看看使用c封装的3个epoll系统调用:

- int epoll_create(int size)

epoll_create建立一个epoll对象。参数size是内核保证能够正确处理的最大句柄数，多于这个最大数时内核可不保证效果。

- int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event)

epoll_ctl可以操作epoll_create创建的epoll，如将socket句柄加入到epoll中让其监控，或把epoll正在监控的某个socket句柄移出epoll。

- int epoll_wait(int epfd, struct epoll_event *events,int maxevents, int timeout)

epoll_wait在调用时，在给定的timeout时间内，所监控的句柄中有事件发生时，就返回用户态的进程。

大概看看epoll内部是怎么实现的：

1. epoll初始化时，会向内核注册一个文件系统，用于存储被监控的句柄文件，调用epoll_create时，会在这个文件系统中创建一个file节点。同时epoll会开辟自己的内核高速缓存区，以红黑树的结构保存句柄，以支持快速的查找、插入、删除。还会再建立一个list链表，用于存储准备就绪的事件。
2. 当执行epoll_ctl时，除了把socket句柄放到epoll文件系统里file对象对应的红黑树上之外，还会给内核中断处理程序注册一个回调函数，告诉内核，如果这个句柄的中断到了，就把它放到准备就绪list链表里。所以，当一个socket上有数据到了，内核在把网卡上的数据copy到内核中后，就把socket插入到就绪链表里。
3. 当epoll_wait调用时，仅仅观察就绪链表里有没有数据，如果有数据就返回，否则就sleep，超时时立刻返回。

epoll的两种工作模式：

- LT：level-trigger，水平触发模式，只要某个socket处于readable/writable状态，无论什么时候进行epoll_wait都会返回该socket。
- ET：edge-trigger，边缘触发模式，只有某个socket从unreadable变为readable或从unwritable变为writable时，epoll_wait才会返回该socket。

socket读数据

![img](http://incdn1.b0.upaiyun.com/2017/08/bc7d7b6c3b3589e8a522196f6de4cfc8.png) 

socket写数据 

![img](http://incdn1.b0.upaiyun.com/2017/08/0b18b383d760281fecb46b4993c6de95.png) 

顺便说下在Linux系统中JDK NIO使用的是 LT ，而Netty epoll使用的是 ET。 



# NIO比BIO效率高原因

网上大多给出了两者的区别，可是具体效率高在哪里呢。

首先我们看一下各自的特点 
**BIO：**

1. socketServer的accept方法是阻塞的。
2. 当有连接请求时，socketServer通过accept方法获取一个socket
3. 取得socket后，将这个socket分给一个线程去处理。此时socket需要等待有效的请求数据到来后，才可以真正开始处理请求。
4. socket交给线程后，这时socketServer才可以接收下一个连接请求。
5. 获得连接的顺序是和客户端请求到达服务器的先后顺序相关。

**NIO：**

1. 基于事件驱动，当有连接请求，会将此连接注册到多路复用器上（selector）。
2. 在多路复用器上可以注册监听事件，比如监听accept、read
3. 通过监听，当真正有请求数据时，才来处理数据。
4. 不会阻塞，会不停的轮询是否有就绪的事件，所以处理顺序和连接请求先后顺序无关，与请求数据到来的先后顺序有关

## 主要对比

这里写的有点问题，NIO处理的时候也需要多个线程的，针对2，那个7秒对java来说是不存在的

- BIO一个连接，一个线程，非http请求，有可能只连接不发请求数据，此时线程是无用浪费的。

- BIO处理依赖于连接建立；NIO处理依赖于请求数据的到来。导致执行顺序不同。

  1. 一个线程处理一个请求 
     **BIO**：连接请求来，建立socket，等待请求数据到来（t1），处理时间（t2） 
     **NIO**：连接请求来，注册到selector，设置读监听，等待请求数据（t1），处理时间（t2） 
     此时，两者用时皆为t1+t2，没有区别
  2. 一个线程处理两个请求 
     第一个请求，等待请求数据（10），处理时间（1） 
     第二个请求，等待请求数据（1），处理时间（2） 
     **BIO**：用时 10+1+1+2=14，第1个执行完用时10+1，等待第一个执行完处理第2个，用时1+2 
     **NIO**：用时 1+2+7+1=11， 第二个数据先到，时间 1+2，此时第一个需要等时为10秒，还没到，还需等待7秒，时间为7+1
  3. 两个线程处理两个请求 
     第一个请求，等待请求数据（10），处理时间（1） 
     第二个请求，等待请求数据（1），处理时间（2） 
     **BIO**：用时 10+1+2=13，等待第1个请求10，交给工作线程一处理，此时同时接受第2个，等待1秒，处理时间2秒，此间线程一处理时间为一秒，在线程二结束之前就已经结束 
     **NIO**：用时 1+2+7+1=11，第二个数据先到，时间 1+2，此时第一个还没到，还需等待7秒，时间为7+1 
     **如果两个请求顺序相反，则bio和nio一样，都是11秒** 
     由此可见由于阻塞等待机制的不同，导致效率不同，主要优化点为，不必排队等待，先到先处理，就有可能效率高一点。

- BIO如果想要处理并发请求，则必须使用多线程，一般后端会用线程池来支持 
  NIO可以使用单线程，可以减少线程切换上下文的消耗。 
  但是虽然单线程减少了线程切换的消耗，但是处理也变为线性的，也就是处理完一个请求，才能处理第二个。 
  这时，有这么两个场景：

  1. 后端是密集型的计算，没有大量的IO操作，比如读些文件、数据库等
  2. 后端是有大量的IO操作。

  当为第一种场景时： 
  NIO单线程则比较有优势， 理由是虽然是单线程，但是由于线程的计算是并发计算，不是并行计算，说到底，计算压力还是在CPU上，一个线程计算，没有线程的多余消耗，显然比NIO多线程要高效。BIO则必为多线程，否则将阻塞到天荒地老，但多线程是并发，不是并行，主要还是依靠CPU的线性计算，另外还有处理大量的线程上下文。 
  如果为第二种场景，多线程将有一定优势，多个线程把等待IO的时间能平均开。此时两者区别主要取决于以上分析的处理顺序了，显然NIO要更胜一筹。

## 总结

NIO在接收请求方式上，无疑是要高效于BIO，原因并非是不阻塞，我认为NIO一样是阻塞的，只是方式不同，先来的有效请求先处理，先阻塞时间短的。此时间可用于等待等待时间长的。 
在处理请求上，NIO和BIO并没有什么不同，主要看线程池规划是否和理。NIO相对BIO在密集型计算的模型下，可以用更少的线程，甚至单线程。





原文地址

Selector 实现原理 http://www.importnew.com/26258.html

Java NIO Selector详解 https://blog.csdn.net/jeffleo/article/details/54695959

 为什么NIO比BIO效率高 https://blog.csdn.net/wy0123/article/details/79382761