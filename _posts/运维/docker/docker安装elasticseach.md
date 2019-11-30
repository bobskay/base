---

title: docker安装elasticsearch
date: 2019-01-11 07:18:01
categories: [elasticseach]
tags: [elasticseach]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

docker安装elasticsearch5.6.8

<!--more-->

# 主程序

docker里的文件

```shell
#主配置
- /opt/temp/elk/es1.yml:/usr/share/elasticsearch/config/elasticsearch.yml
#jvm参数,默认2g虚拟机内存不够
- /opt/temp/elk/jvm.options:/etc/elasticsearch/jvm.options
#插件
/opt/temp/elk/plugins:/usr/share/elasticsearch/plugins
#数据
- /home/data/elk/es1:/usr/share/elasticsearch/data
#日志
- /home/logs/es1:/usr/share/elasticsearch/logs
```

elasticsearch不能用root启动，所以需要将外部文件权限修改为其它用户能读的，因为当前目录是windows 挂载目录，无法修改文件权限，所以需要将其复制到其它地方

```shell
#!/bin/sh
docker-compose stop
rm -rf /opt/temp/elk
mkdir -p /opt/temp/elk/plugins
cp ./* /opt/temp/elk/
unzip ./plugins/ik.zip -d /opt/temp/elk/plugins/ik
chmod 777 /opt/temp/elk/*
docker-compose up -d
```

启动的时候看到jvm的参数是改过的，就说明配置生效了

![1570691729920](/blog/images/1570691729920.png)

# ik分词器

到作者GitHub下载对应版本

```shell
https://github.com/medcl/elasticsearch-analysis-ik/releases
```

将压缩包到linux目录，然后启动docker的时候做 映射就可以了

```shell
/opt/temp/elk/plugins:/usr/share/elasticsearch/plugins
```

分词测试

```shell
http://es1:9200/_analyze?analyzer=ik_smart&text=南京市长江大桥
```
# kibana

kibana比较简单，直接pull对应版本，启动的时候设置elasticsearch地址就可以了

```
ELASTICSEARCH_URL=http://es1:9200
```