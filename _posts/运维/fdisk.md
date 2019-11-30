---
title: linux磁盘分区
date: 2019-02-03 12:41:14
categories: [linux]
tags: [fdisk]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

 linux磁盘分区

<!--more-->

查看为分配的磁盘

```shell
fdisk -l
```

![1549168894081](/blog/images/1549168894081.png)

/dev/sdb 在文件系统里没有,说明未初始化

```shell
#开始分区
fdisk /dev/sdb
#依次输入
n 
p
1
w

```

![1549169224094](/blog/images/1549169224094.png)

查看新创建的磁盘

```shell
ls /dev/sd*
#多出2个/dev/sdb和/dev/sdb1
```



![1549169438377](/blog/images/1549169438377.png)

挂载

```shell
#格式化
mkfs.ext4 /dev/sdb1 
#创建目标目录
mkdir /common
#mount -t 表示文件系统
mount -t ext4 /dev/sdb1 /common 
```

开机挂载

```shell
vim /etc/fstab  
/dev/sdb1 /common ext4 errors=remount-ro 0 1 
```

