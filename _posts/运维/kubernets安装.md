---
title: kubernetes安装
date: 2019-02-02 15:54:31
categories: [kubernets]
tags: [kubernets]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

kuberneteskubernetes

<!--more-->

准备工作

master01:192.168.1.110

node01:192.168.1.106

关闭防火墙

```shell
systemctl disable firewalld
iptables -F
```

yum安装

master

192.168.1.110

```shell
yum install kubernetes-master etcd -y
```

配置

vim /etc/etcd/etcd.conf

```properties
ETCD_DATA_DIR="/var/lib/etcd/abc"
ETCD_LISTEN_PEER_URLS="http://192.168.1.110:2380"
ETCD_LISTEN_CLIENT_URLS="http://192.168.1.110:2379"
ETCD_NAME="etcd1"
ETCD_INITIAL_ADVERTISE_PEER_URLS="http://192.168.1.110:2380"
ETCD_ADVERTISE_CLIENT_URLS="http://192.168.1.110:2379"
ETCD_INITIAL_CLUSTER="etcd1=http://192.168.1.110:2380,etcd2=http://192.168.1.106:2380"
```

vim  /etc/kubernetes/apiserver

```properties
KUBE_API_ADDRESS="--insecure-bind-address=127.0.0.1"
KUBE_ETCD_SERVERS="--etcd-servers=http://192.168.1.110:2379,http://192.168.1.106:2379"
KUBE_SERVICE_ADDRESSES="--service-cluster-ip-range=10.254.0.0/16"
KUBE_API_ARGS=""               
```

vim /etc/kubernetes/config

```properties
KUBE_LOGTOSTDERR="--logtostderr=true"
KUBE_LOG_LEVEL="--v=0"
KUBE_ALLOW_PRIV="--allow-privileged=false"
KUBE_MASTER="--master=http://192.168.1.110:8080"
```

重启

```shell
#停止
systemctl stop etcd
systemctl stop kube-apiserver
systemctl stop kube-controller-manager
systemctl stop kube-scheduler
#启动
systemctl daemon-reload
systemctl restart etcd
systemctl restart kube-apiserver
systemctl restart kube-controller-manager
systemctl restart kube-scheduler
```

查看

```shell
kubectl get nodes --insecure-skip-tls-verify=true
```

node

192.168.1.105

安装

```shell
yum install kubernetes-node etcd flannel -y
```

配置

vim /etc/etcd/etcd.conf

```properties
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="http://192.168.1.106:2380"
ETCD_LISTEN_CLIENT_URLS="http://192.168.1.106:2379,http://127.0.0.1:2379"
ETCD_NAME="etcd2"
ETCD_INITIAL_ADVERTISE_PEER_URLS="http://192.168.1.106:2380"
ETCD_ADVERTISE_CLIENT_URLS="http://192.168.1.106:2379"
ETCD_INITIAL_CLUSTER="etcd1=http://192.168.1.110:2380,etcd2=http://192.168.1.106:2380"
```

vim /etc/kubernetes/kubelet

```properties
KUBELET_ADDRESS="--address=0.0.0.0"
KUBELET_HOSTNAME="--hostname-override=node01.kube.wang"
KUBELET_API_SERVER="--api-servers=http://192.168.1.110:8080"
KUBELET_POD_INFRA_CONTAINER="--pod-infra-container-image=registry.access.redhat.com/rhel7/pod-infrastructure:latest"
KUBELET_ARGS=""

```

启动

```shell
systemctl daemon-reload
systemctl restart kube-proxy
systemctl restart kubelet
```

客户端设置kubectl

```shell
kubectl config set-cluster kubernetes --server=http://192.168.1.110:8080
kubectl config set-context kubernetes --cluster=kubernetes
kubectl config use-context kubernetes
```



dashboard

```shell
docker pull docker.io/mirrorgooglecontainers/kubernetes-dashboard-amd64:v1.10.1
docker tag docker.io/mirrorgooglecontainers/kubernetes-dashboard-amd64:v1.10.1  k8s.gcr.io/kubernetes-dashboard-amd64:v1.10.1
```



etcd

启动慢

​	确认防火墙是否关闭

​	确认ETCD_INITIAL_CLUSTER里所有机器都启动

member 73079f127bc11ea8 has already been bootstrapped





错误

```verilog
Error syncing pod, skipping: failed to "StartContainer" for "POD" with ErrImagePull: "image pull failed for registry.access.redhat.com/rhel7/pod-infrastructure:latest, this may be because there are no credentials on this request.  details: (open /etc/docker/certs.d/registry.access.redhat.com/redhat-ca.crt: no such file or directory)"
```

解决办法

```shell
wget http://mirror.centos.org/centos/7/os/x86_64/Packages/python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm
rpm2cpio python-rhsm-certificates-1.19.10-1.el7_4.x86_64.rpm | cpio -iv --to-stdout ./etc/rhsm/ca/redhat-uep.pem | tee /etc/rhsm/ca/redhat-uep.pem   
docker pull registry.access.redhat.com/rhel7/pod-infrastructure:latest
```

