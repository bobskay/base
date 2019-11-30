---

title: elasticsearch
date: 2019-01-11 07:18:01
categories: [elasticseach]
tags: [elasticseach]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

elasticsearch使用

<!--more-->

# 安装

## elasticsearch

https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.0.zip

修改配置

config/elasticsearch.yml

```yaml
http.cors.enabled: true 
http.cors.allow-origin: "*"
node.master: true
node.data: true
network.host: 0.0.0.0
cluster.name: my-application
node.name: node-1

```

## 中文分词器

https://codeload.github.com/medcl/elasticsearch-analysis-ik/zip/master

```shell
E:\soft\elasticseach\elasticsearch-6.3.0\bin>elasticsearch-plugin.bat install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.3.0/elasticsearch-analysis-ik-6.3.0.zip
```

测试

```shell
GET _analyze?pretty
{
  "analyzer": "ik_smart",
  "text": "中华人民共和国国歌"
}
```



## elasticsearch-head

https://codeload.github.com/mobz/elasticsearch-head/zip/v5.0.0

```shell
cd E:\soft\elasticseach\elasticsearch-head-5.0.0
npm install -g grunt-cli
npm install
```



# 启动

## elasticsearch

```shell
e:
cd E:\soft\elasticseach\elasticsearch-6.3.0\bin
elasticsearch.bat
```

http://127.0.0.1:9200/

## kibana

```shell
e:
cd E:\soft\elasticseach\kibana-6.2.4-windows-x86_64\bin
kibana.bat
```

http://127.0.0.1:5601

## elasticsearch-head

```shell
cd E:\soft\elasticseach\elasticsearch-head-5.0.0\
grunt server
```

http://127.0.0.1:9100/



# 基本操作

## 查看状态

```shell
GET _cat/health?v
```

```tex
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1547428524 01:15:24  elasticsearch green           1         1      0   0    0    0        0             0                  -                100.0%
```

## 增

### 设置mapping

```json
PUT store
{
  "mappings":{
    "book": {
       "properties": {
          "bookId": {"type": "long"},
          "bookName": {"type": "text"},
          "publishDate": {
			"type": "date",
			"format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
		  },
		  "description": {
            "type": "text",
			"analyzer": "ik_max_word"
          },
		  "isbn": {"type": "text"},
		  "tags": {
			"type":"keyword"
		  },
		  "price":{"type":"long"}
       }
    }
  }
}
```

### 新增数据

```json
PUT /store/book/1
{
	"bookId":1,
	"bookName":"深入理解虚拟机",
	"description":"售价100的java图书,包含虚拟机",
	"isbn":"11233123",
	"price":100,
	"publishDate":1547430116594,
	"tags":["java","虚拟机"]
}

PUT /store/book/2
{
	"bookId":2,
	"bookName":"java最佳实践",
	"description":"售价200的java图书",
	"isbn":"11233123",
	"price":200,
	"publishDate":1547430116594,
	"tags":["java"]
}

PUT /store/book/3
{
	"bookId":2,
	"bookName":"设计模式与java",
	"description":"售价300的java图书,关于设计模式的",
	"isbn":"11233123",
	"price":200,
	"publishDate":1547430116594,
	"tags":["java","设计模式"]
}

```

## 删

## 改

```json
POST /store/book/1/_update
{
  "doc": 
  {
	  "description":"更新后的描述信息"
  }
}
```



## 查

### 查询全部

```shell
GET /store/book/_search
```

```json
GET /store/book/_search
{
  "query": {"match_all":{}}
}
```

### 按字段匹配，排序

```shell
GET /store/book/_search
{
  "query": 
  {
    "match": 
    {
      "tags": "java"
    }
    
  },
  "sort": [
    {
      "price": {
        "order": "desc"
      }
    }
  ]
}
```

### 多条件

```json
GET /store/book/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
          "tags": "java"
          }
        }
      ],
      "filter": {
        "range": {
          "price": {
            "gte": 100,
            "lte": 500
          }
        }
      }
    }
  }
}
```



### 分页

from,size 相当于 mysql的limit

```json
GET /store/book/_search
{
  "query": {"match_all":{}},
  "from": 0,
  "size": 1
}
```

### 查询部分字段

```json
GET /store/book/_search
{
  "query": {"match_all":{}},
  "_source": ["bookId","bookName"]
}
```

### 全文检索

描述包含"售价 设计模式"排最前面

```json
GET /store/book/_search
{
  "query": {
    "match": {
      "description": "售价 设计模式"
    }
  }
}
```

### 高亮

高亮描述字段

```json
GET /store/book/_search
{
  "query": {
    "match": {
      "description": "售价 虚拟机"
    }
  },
  "highlight": {
    "fields": {
      "description": {}
    }
  }
}
```

## 统计

### 修改字段属性为可以统计

```json
PUT /store/_mapping/book
{
  "properties": {
    "tags":{
      "type": "text",
      "fielddata": true
    }
  }
}
```

### groupby

```sql
select tags, count(*) from book group by tags
```

```json
GET /store/book/_search
{
  "aggs": {
    "mygroup": {
      "terms": {
        "field": "tags"
      }
    }
  },
  "size": 0
}
```

### avg

```sql
select tags,count(*),avg(price) from book group by tags
```

```json
GET /store/book/_search
{
  "aggs": {
    "groupbytags": {
      "terms": {
        "field": "tags"
      },
      "aggs": {
        "avgprice": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  },
  "size": 0
}
```

### orderby

```sql
select tags,count(*),avg(price) avgprice pp from book group by tags order by avgprice
```

```json
GET /store/book/_search
{
  "aggs": {
    "groupbytags": {
      "terms": {
        "field": "tags",
        "order": {
          "avgprice": "asc"
        }
      },
      "aggs": {
        "avgprice": {
          "avg": {
            "field": "price"
          }
        }
      }
    }
  },
  "size": 0
}

```

### case

```sql
select count(case when price>=0 and preie<100 then tags,
             case when price>=100 and preie<200 then tags
            ) from book 
```

```json
GET /store/book/_search
{
  "aggs": {
    "statprice": {
      "range": {
        "field": "price",
        "ranges": [
          {
            "from":0,
            "to": 100
          },
          {
            "from":100,
            "to": 200
          },
          {
            "from":200,
            "to": 300
          }
        ]
      },
      "aggs": {
        "groupbytags": {
          "terms": {
            "field": "tags"
          }
        }
      }
    }
  },
  "size": 0
}

```
