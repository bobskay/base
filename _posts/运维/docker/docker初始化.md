---
title: docker初始化
date: 2019-02-03 12:26:33
categories: [docker]
tags: [docker]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

docker初始化

<!--more-->

# 安装

```shell
yum install docker -y
```

修改docker目录

 ```shell
docker info |grep Dir
 ```



![1549169899721](/blog/images/1549169899721.png)

```shell
#停止服务
#将文件复制到目标目录
#删除源文件
#创建软链
#重启
systemctl stop docker
cp -rf /var/lib/docker/ /home/docker/
rm -rf /var/lib/docker/
ln -sd /home/docker/ /var/lib/docker
systemctl start docker
```

# 镜像加速器

```shell
mkdir -p /etc/docker
tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://jyvpxfwu.mirror.aliyuncs.com"]
}
EOF
```

# 重启

```shell
systemctl daemon-reload
systemctl restart docker
```

# 测试

```shell
docker pull busybox
docker run --rm -it busybox
```

# 管理界面

```shell
docker pull
docker run -d -p 9000:9000 \
    --restart=always \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name prtainer-test \
   --privileged=true \
    portainer/portainer
```

# 安装docker-compose

## 安装python3

```shell
yum install python3
```

## 关联包

```cmd
正在安装:
 python3     x86_64         3.6.8-10.el7   base  69 k
为依赖而安装:
 python3-libs       x86_64         3.6.8-10.el7   base         7.0 M
 python3-pip        noarch         9.0.3-5.el7    base         1.8 M
 python3-setuptools         noarch         39.2.0-10.el7  base         629 k
```

## 修改python的软链为python3

```shell
rm -rf /usr/bin/python
ln -s /usr/bin/python3.4 /usr/bin/python
```

### yum无法使用

修改以下两个文件的python声明为2.7

```cmd
#!/usr/bin/python2.7
vim /usr/bin/yum 
vim /usr/libexec/urlgrabber-ext-down 
```

## 安装docker-compose

```shell
pip3.4 install  --upgrade pip
pip3.4 install docker-compose
```

# 监控

## cadvisor

### 运行容器

```shell
docker run --privileged -v /var/run:/var/run:rw   -v /sys:/sys:ro   -v /var/lib/docker:/var/lib/docker:ro  -p 8080:8080  -d  --name cadvisor   google/cadvisor
```

### 可能报错

```
Failed to start container manager: inotify_add_watch
/sys/fs/cgroup/cpuacct,cpu: no such file or directory
```

### 解决办法

```shell
mount -o remount,rw '/sys/fs/cgroup'
ln -s /sys/fs/cgroup/cpu,cpuacct /sys/fs/cgroup/cpuacct,cpu
```

