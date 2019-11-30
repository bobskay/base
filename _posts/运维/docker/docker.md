---
title: docker
date: 2018-08-22 07:07:08
categories: [docker]
tags: []
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

docker

<!--more-->

转自 https://blog.csdn.net/birdben/article/details/49873725

## 基本命令

```shell
# 在ubuntu中安装docker
$ sudo apt-get install docker.io

# 查看docker的版本信息
$ docker version

# 查看安装docker的信息
$ docker info

# 查看本机Docker中存在哪些镜像
$ docker images

# 检索image
$ docker search ubuntu:14.04

# 在docker中获取ubuntu镜像
$ docker pull ubuntu:14.04

# 显示一个镜像的历史
$ docker history birdben/ubuntu:v1

# 列出一个容器里面被改变的文件或者目
$ docker diff birdben/ubuntu:v1

# 从一个容器中取日志
$ docker logs birdben/ubuntu:v1

# 显示一个运行的容器里面的进程信息
$ docker top birdben/ubuntu:v1

# 从容器里面拷贝文件/目录到本地一个路径
$ docker cp ID:/container_path to_path

# 列出当前所有正在运行的容器
$ docker ps

# 列出所有的容器
$ docker ps -a

# 列出最近一次启动的容器
$ docker ps -l

# 查看容器的相关信息
$ docker inspect $CONTAINER_ID

# 显示容器IP地址和端口号，如果输出是空的说明没有配置IP地址（不同的Docker容器可以通过此IP地址互相访问）
$ docker inspect --format='{{.NetworkSettings.IPAddress}}' $CONTAINER_ID

# 保存对容器的修改 
$ docker commit -m "Added ssh from ubuntu14.04" -a "birdben" 6s56d43f627f3 birdben/ubuntu:v1

# 参数：
# -m参数用来来指定提交的说明信息；
# -a可以指定用户信息的；
# 6s56d43f627f3代表的时容器的id；
# birdben/ubuntu:v1指定目标镜像的用户名、仓库名和 tag 信息。

# 构建一个容器 
$ docker build -t="birdben/ubuntu:v1" .

# 参数：
# -t为构建的镜像制定一个标签，便于记忆/索引等
# . 指定Dockerfile文件在当前目录下，也可以替换为一个具体的 Dockerfile 的路径。

# 在docker中运行ubuntu镜像
$ docker run <相关参数> <镜像 ID> <初始命令>

# 守护模式启动
$ docker run -it ubuntu:14.04

# 交互模式启动
$ docker run -it ubuntu:14.04 /bin/bash

# 指定端口号启动
$ docker run -p 80:80 birdben/ubuntu:v1

# 指定配置启动
$ sudo docker run -d -p 10.211.55.4:9999:22 birdben/ubuntu:v1 '/usr/sbin/sshd' -D

# 参数：
# -d：表示以“守护模式”执行，日志不会出现在输出终端上。
# -i：表示以“交互模式”运行容器，-i 则让容器的标准输入保持打开
# -t：表示容器启动后会进入其命令行，-t 选项让Docker分配一个伪终端（pseudo-tty）并绑定到容器的标准输入上
# -v：表示需要将本地哪个目录挂载到容器中，格式：-v <宿主机目录>:<容器目录>，-v 标记来创建一个数据卷并挂载到容器里。在一次 run 中多次使用可以挂载多个数据卷。
# -p：表示宿主机与容器的端口映射，此时将容器内部的 22 端口映射为宿主机的 9999 端口，这样就向外界暴露了 9999 端口，可通过 Docker 网桥来访问容器内部的 22 端口了。
# 注意：这里使用的是宿主机的 IP 地址：10.211.55.4，与对外暴露的端口号 9999，它映射容器内部的端口号 22。ssh外部需要访问：ssh root@10.211.55.4 -p 9999
# 不一定要使用“镜像 ID”，也可以使用“仓库名:标签名”

# start 启动容器
$ docker start 117843ade696117843ade696
# stop 停止正在运行的容器
$ docker stop 117843ade696117843ade696
# restart 重启容器
$ docker restart 117843ade696117843ade696
# rm 删除容器
$ docker rm 117843ade696117843ade696
# rmi 删除镜像
$ docker rmi ed9c93747fe1Deleted

# 登录Docker Hub中心
$ docker login

# 发布上传image（push）
$ docker push birdben/ubuntu:v1

#进入容器
$ ocker exec -it zookeeper_zoo3_1 bash
```

当利用 docker run 来创建容器时，Docker 在后台运行的标准操作包括：

- 检查本地是否存在指定的镜像，不存在就从公有仓库下载
- 利用镜像创建并启动一个容器
- 分配一个文件系统，并在只读的镜像层外面挂载一层可读写层
- 从宿主主机配置的网桥接口中桥接一个虚拟接口到容器中去
- 从地址池配置一个 ip 地址给容器
- 执行用户指定的应用程序
- 执行完毕后容器被终止

## 制作镜像

### 2.使用docker commit 命令来创建镜像

1. 通过docker run命令启动容器
2. 修改docker镜像内容
3. docker commit提交修改的镜像
4. docker run新的镜像

### 2.使用 Dockerfile 来创建镜像

Dockerfile

```shell

# 生成的新镜像以centos镜像为基础
FROM centos
# 指定作者信息
MAINTAINER wangym
# 安装openssh-server
RUN yum -y install openssh-server

RUN mkdir /var/run/sshd
RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key

# 指定root密码
RUN /bin/echo 'root:123456'|chpasswd
RUN /bin/sed -i 's/.*session.*required.*pam_loginuid.so.*/session optional pam_loginuid.so/g' /etc/pam.d/sshd
RUN /bin/echo -e "LANG=\"en_US.UTF-8\"" > /etc/default/local
EXPOSE 22
CMD /usr/sbin/sshd -D
```

运行

```shell
docker build -t my/centos-ssh:1.0 .
```

