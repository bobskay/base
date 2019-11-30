---
title: Prometheus入门
date: 2019-10-07 17:49:41
categories: [监控]
tags: [Prometheus,grafana]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---


 Prometheus安装和基础配置，包括node-exporter，pushgateway和grafana的使用

<!--more-->

# 安装

## Prometheus

数据存放路径：/home/data/prometheus

配置文件：prometheus.yml

```yml
version: '3'
services:
  prometheus:
    privileged: true
    image: prom/prometheus
    container_name: prometheus
    user: root
    volumes:
      - /etc/hosts:/etc/hosts
      - /home/data/prometheus:/prometheus
      - $PWD/prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - 9090:9090
```

因为发现容器内无法访问本地文件，所以添加user: root

## node-exporter

```yml
version: '3'
services:
  nodeexporter:
    image: prom/node-exporter
    container_name: nodeexporter
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    ports:
      - 9100:9100

```

## pushgateway

```yml
version: '3'
services:
  pushgateway:
    image: prom/pushgateway
    container_name: pushgateway
    volumes:
      - /etc/hosts:/etc/hosts
    ports:
      - 9091:9091
```

# 通过node-exporter获取数据

## 配置

prometheus.yml添加

```yml
  - job_name: linux
    static_configs:
      - targets: ['myos:9100']
        labels:
          instance: myos
```

myos:9100为node-exporter的访问地址

## 测试

输入地址：http://myos:9090/graph

![1570465141794](/blog/images/1570465141794.png)

通过以下公式得到cpu占用百分比
```shell
(1-(sum(increase(node_cpu_seconds_total{mode="idle"}[5m])) by (instance) )/(sum(increase(node_cpu_seconds_total[5m])) by (instance) ))*100
```

# 通过pushgateway获取数据

## 配置

prometheus.yml添加

```yml
  - job_name: pushgateway
    static_configs:
      - targets: ['myos:9091']
        labels:
          instance: pushgateway
```

## 测试

### 添加单条

```shell
echo "some_metric 3.14" | curl --data-binary @- http://myos:9091/metrics/job/myjob/instance/myinstance
```

### 添加多条

通过postman发送post

![1570465551461](/blog/images/1570465551461.png)

成功在prometheus里就会出现相应的数据

![1570465634137](/blog/images/1570465634137.png)
数据会自动生成一些标签
exported_instance和exported_job根据url里的内容生成
host和url是发送数据时自己写的标签

```shell
http_request{exported_instance="myinstance",exported_job="myjob",host="web1",instance="pushgateway",job="pushgateway",url="/book/select"}
```

# grafana

## 安装

数据存放路径：/home/data/grafana

```yml
version: '3'
services:
  grafana:
    privileged: true
    image: grafana/grafana:4.0.2
    container_name: grafana
    user: root
    volumes:
      - /home/data/grafana:/var/lib/grafana
      - /etc/hosts:/etc/hosts
    ports:
      - 3000:3000
```

访问地址

[http://myos:3000](http://myos:3000/)

用户名密码

admin/admin

## 添加数据源

url填写prometheus的访问路径：[http://myos:9090](http://myos:9090/)

![1570466014631](/blog/images/1570466078730.png)

## 配置报警接收器

![1570466629406](/blog/images/1570466629406.png)

如果用webhook接收报警信息，点击测试可以在post的body里收到如下信息

```js
{
			"imageUrl":"http://grafana.org/assets/img/blog/mixed_styles.png",
			"ruleName":"Test notification",
			"state":"alerting",
			"message":"Someone is testing the alert notification within grafana.",
			"ruleId":0,
			"title":"[Alerting] Test notification",
			"ruleUrl":"http://localhost:3000/",
			"evalMatches":[
				{
					"metric":"High value",
					"value":100
				},
				{
					"metric":"Higher Value",
					"value":200
				}
			]
}
```

## 添加监控图

新增dashboard-->选择graph-->点击panel title-->选择edit

![1570466874662](/blog/images/1570466874662.png)

query输入内容和Prometheus里的相同

数据源选择刚才添加的数据源

![1570467073723](/blog/images/1570467073723.png)

alert配置，每分钟检查1此如果最大值大于50就报警

![1570467291591](/blog/images/1570467291591.png)

send to选择刚才配置的报警接收器

![1570467376019](/blog/images/1570467376019.png)

最终显示效果

![1570469170274](/blog/images/1570469245788.png)

实际收到的报警信息，state=alerting，当cpu恢复正常后会收到一条相同的信息，但state=ok

```js
//报警
{
			"ruleName":"cpu使用率报警",
			"state":"alerting",
			"message":"cpu占用过高",
			"ruleId":1,
			"title":"[Alerting] cpu使用率报警",
			"ruleUrl":"http://localhost:3000/dashboard/db/cpushi-yong-lu?fullscreen&edit&tab=alert&panelId=1",
			"evalMatches":[
				{
					"metric":"myos",
					"value":67.42521121150497
				}
			]
		}
//恢复

{
			"ruleName":"cpu使用率报警",
			"state":"ok",
			"message":"cpu占用过高",
			"ruleId":1,
			"title":"[OK] cpu使用率报警",
			"ruleUrl":"http://localhost:3000/dashboard/db/cpushi-yong-lu?fullscreen&edit&tab=alert&panelId=1",
			"evalMatches":[]
}
```

