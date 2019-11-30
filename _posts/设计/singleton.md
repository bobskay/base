---
title: Singleton的double check问题
date: 2019-01-17 06:24:56
categories: [design]
tags: [设计,多线程,单例]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

创建单例对象如果用double check方式，会存在不安全的情况，简单说法是给对象加上volatile关键字，

具体原因牵扯到比较多，下面具体说明，先看代码

# 代码

创建10个线程，同时调用getInstance()方法获取单例对象

getInstance方法，创建对象的过程为

1.判断对象是否为null

2.获得锁

3.再次判断对象是否为null

4.创建对象

5.初始化对象内部数据

```java
package thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Singleton {
    private static Singleton singleton;
    private String initThreadName;//记录单例对象是由哪个线程创建的

    public static Singleton getInstance() throws InterruptedException {
        if (singleton != null) {//1
            debug("singleton不为null,直接返回");
            return singleton;
        }
        debug("singleton为null,准备获得锁");
        synchronized (Singleton.class) {//2
            if (singleton != null) {//3
                debug("获得锁后发现singleton已经创建,直接返回");
                return singleton;
            }
            debug("singleton为null,开始执行创建");
            singleton = new Singleton();//4
            Thread.sleep(2000);//假装对象创建比较耗时
            singleton.initThreadName = Thread.currentThread().getName();//5
            debug("-----singleton创建完毕-------");
            return singleton;
        }
    }
    
    //创建10个线程,同时调用getInstance()
    public static void main(String args[]) throws InterruptedException {
        List<Thread> list = new ArrayList<>();
        CountDownLatch c = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            Runnable run = () -> {
                try {
                    debug("线程创建完毕等待执行");
                    c.await();//等待线程全部到达
                    debug("准备获取对象");
                    Singleton singleton = Singleton.getInstance();
                    debug("获得单例对象,该对象是由线程" + singleton.initThreadName + "创建的");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            };
            new Thread(run, "thread" + i).start();
        }
        Thread.sleep(100);
        c.countDown();
        Thread.sleep(5000);
        Singleton.getInstance();
    }

    static long start = System.currentTimeMillis();//记录启动时间
    public static void debug(String msg) {
        System.out.println(System.currentTimeMillis() - start + "|" + Thread.currentThread().getName() + ":" + msg);
    }
}

```

# 执行日志

```cmd
48|thread0:线程创建完毕等待执行
48|thread2:线程创建完毕等待执行
48|thread1:线程创建完毕等待执行
48|thread3:线程创建完毕等待执行
48|thread4:线程创建完毕等待执行
48|thread5:线程创建完毕等待执行
48|thread6:线程创建完毕等待执行
48|thread9:线程创建完毕等待执行
49|thread7:线程创建完毕等待执行
49|thread8:线程创建完毕等待执行
149|thread0:准备获取对象
149|thread1:准备获取对象
149|thread5:准备获取对象
149|thread9:准备获取对象
149|thread9:singleton为null,准备获得锁
149|thread9:singleton为null,开始执行创建
149|thread2:准备获取对象
149|thread2:singleton不为null,直接返回
149|thread2:获得单例对象,该对象是由线程null创建的
149|thread8:准备获取对象
149|thread7:准备获取对象
149|thread5:singleton为null,准备获得锁
149|thread6:准备获取对象
150|thread6:singleton不为null,直接返回
150|thread6:获得单例对象,该对象是由线程null创建的
149|thread1:singleton为null,准备获得锁
149|thread4:准备获取对象
150|thread4:singleton不为null,直接返回
150|thread4:获得单例对象,该对象是由线程null创建的
149|thread0:singleton为null,准备获得锁
149|thread3:准备获取对象
150|thread3:singleton不为null,直接返回
149|thread7:singleton不为null,直接返回
150|thread7:获得单例对象,该对象是由线程null创建的
149|thread8:singleton不为null,直接返回
150|thread3:获得单例对象,该对象是由线程null创建的
150|thread8:获得单例对象,该对象是由线程null创建的
2150|thread9:-----singleton创建完毕-------
2150|thread9:获得单例对象,该对象是由线程thread9创建的
2150|thread0:获得锁后发现singleton已经创建,直接返回
2150|thread0:获得单例对象,该对象是由线程thread9创建的
2150|thread1:获得锁后发现singleton已经创建,直接返回
2150|thread1:获得单例对象,该对象是由线程thread9创建的
2150|thread5:获得锁后发现singleton已经创建,直接返回
2150|thread5:获得单例对象,该对象是由线程thread9创建的
5150|main:singleton不为null,直接返回
5162|main:获得单例对象,该对象是由线程thread9创建的
```

# 实际执行过程

## 第一批线程

thread9获得锁，执行到第4步后开始等待

## 第二批线程

thread2，thread6，thread4，thread3，thread7，thread8执行到第1步发现对象不为null，就直接返回了，但这时候initThreadName属性还没赋值，所以拿到对象后打印出来的信息为

```cmd
singleton不为null,直接返回
获得单例对象,该对象是由线程null创建的
```

## 第三批线程

thread0,thread1,thread5执行到第2步后开始等待，等到thread9释放锁后在第3步返回，因此他们得到的信息是正确的

```cmd
获得锁后发现singleton已经创建,直接返回
获得单例对象,该对象是由线程thread9创建的
```

# 问题

第二批线程获取对象的时候，虽然singleton不为null，但里面的属性还未赋值，所以拿到对象后立即操作就会出现问题，如前所述发现initThreadName是null

# 解决办法

创建一个临时对象，先初始化完毕后再赋值给singleton，简单来说就是讲4，5两步对调

```java
//原代码
singleton = new Singleton();//4
singleton.initThreadName = Thread.currentThread().getName();//5

//新代码
Singleton temp= new Singleton();
temp.initThreadName = Thread.currentThread().getName();//5
singleton=temp;//4
```

通常来说，大部分情况下是没问题的，但理论上不是这样，写线程和读线程看到的singleton对象，对于计算机来说不是同一个

```java
//写线程，在写线程的工作内存为singleton赋值后，需要将这份数据赋值到主内存
singleton=temp;
//读线程，需要将主内存的数据复制到当前工作线程，然后才能判断数据是否为null
if (singleton != null) {
```

这里牵扯到java内存模型的问题，java线程是不能直接操作主内存的，每个线程都只能修改自己工作内存，然后将值复制到主内存

![这里写图片描述](/blog/images/20160127101418421)

singleton=temp虽然只有一句，但需要在2个地方写数据，再加上写操作本身就需要store和write

```tex
JAVA内存模型规定工作内存与主内存之间的交互协议，其中包括8种原子操作：
1）lock(锁定)：将一个变量标识为被一个线程独占状态。
2）unlock(解锁)：将一个变量从独占状态释放出来，释放后的变量才可以被其他线程锁定。
3）read(读取)：将一个变量的值从主内存传输到工作内存中，以便随后的load操作。
4）load(载入)：把read操作从主内存中得到的变量值放入工作内存的变量的副本中。
5）use(使用)：把工作内存中的一个变量的值传给执行引擎，每当虚拟机遇到一个使用到变量的指令时都会使用该指令。
6）assign(赋值)：把一个从执行引擎接收到的值赋给工作内存中的变量，每当虚拟机遇到一个给变量赋值的指令时，都要使用该操作。
7）store(存储)：把工作内存中的一个变量的值传递给主内存，以便随后的write操作。
8）write(写入)：把store操作从工作内存中得到的变量的值写到主内存中的变量
```

简单来说，多线程环境下，一个线程修改了singleton的信息，另一个线程并不能马上知道完整信息，理论上虽然

if (singleton != null) 判断为true，但线程所获得的singleton 仍然有可能是不完整的

## Volatile测试

```java
package thread;

public class VolatileTest {
 
    private static  int value = 0;//不加volatile关键字,监听线程是感知不到value值变化的
    public static void main(String[] args) {
        Runnable change=()->{
            try{
                while (value<5){
                    Thread.sleep(100);
                    value++;
                    System.out.println("修改线程将value修改为:"+value);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        };
        Runnable chanListener=()->{
            int local=value;
            while (value < 5) {
                if(local!=value){
                    System.out.println("监听线程发现value已经改变:" +value+"!="+local);
                    local = value;
                }
            }
        };
        new Thread(chanListener,"chanListener").start();
        new Thread(change,"change").start();

    }
}
```

# 总结

所以使用double check方式生成单例需要注意

1.singleton所有初始化工作需要在赋值之前

2.singleton变量需要加上volatile关键字

```java
public class Singleton {
    private static volatile Singleton singleton;//变量设置为volatile
    ...
    public static Singleton getInstance() {
    	...
        Singleton temp= new Singleton();
        //初始化所需的其它字段
        ...
        singleton=temp;//将完整对象赋值给singleton
    }         
}
```

当然创建单例的方式有很多种，实际工作中可以用恶汉模式，或者内部类方式，通过内部类方式实现代码如下

```java
package thread.innerclass;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Singleton {
    private String initThreadName;
    private Singleton() {
    }

    private static class SingletonHandler {
        private static Singleton singleton = init();
        private static Singleton init(){
            System.out.println("初始化singleton");
            Singleton singleton= new Singleton();
            singleton.initThreadName=Thread.currentThread().getName();
            return singleton;
        }
    }
    public static Singleton getInstance() {
        return SingletonHandler.singleton;
    }

    public static void main(String args[]) throws InterruptedException {
        debug("只执行静态方法是不会加载对象的");

        List<Thread> list = new ArrayList<>();
        CountDownLatch c = new CountDownLatch(1);
        for (int i = 0; i < 10; i++) {
            Runnable run = () -> {
                try {
                    debug("线程创建完毕等待执行");
                    c.await();//等待线程全部到达
                    debug("准备获取对象");
                    Singleton singleton = getInstance();
                    debug("获得单例对象,该对象是由线程" + singleton.initThreadName + "创建的");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            new Thread(run, "thread" + i).start();
        }
        Thread.sleep(100);
        c.countDown();
    }

    static long start = System.currentTimeMillis();//记录启动时间
    public static void debug(String msg) {
        System.out.println(System.currentTimeMillis() - start + "|" + Thread.currentThread().getName() + ":" + msg);
    }
}

```





<!--more-->