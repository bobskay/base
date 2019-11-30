---
title: mysql执行计划
date: 2019-10-12 16:39:19
categories: [sql]
tags: [sql,explain]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

mysq explain说明

<!--more-->

# 表头

| id | select_type | table | type | possible_keys | key  | key_len | ref  | rows | Extra |
|---- |----| ---- | ---- | ---- | ---| --- | ---  | --- | --- |
| || ||  | |  | || |

## id

先执行数字高的，数字相同按顺子执行

## select_type

SIMPLE：普通查询

PRIMARY：包含子查询的时候的主查询
SUBQUERY：子查询
DERIVED：临时表

## table

表名或者DERIVED<ID>，ID表示是哪个临时表

## type

优先级

system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL



system：只有1条记录的表查询

const : 通过主键或唯一索引查询

eq_ref：关联查询时其它表用到唯一索引

ref : 通过普通索引

range : 通过索引范围查询

index：全索引扫，返回的字段都在索引里

ALL：全表扫描

## key

实际使用到的索引

## key_len

用到的索引长度，联合索引的情况下如果只用第一个字段查询用到的索引长度会比较全部字段短

## ref

查询时使用的实际值，可以是常量，关联查询时显示另一张表的某列

## rows

实际可能查询的条数

## Extra

Using filesort：排序时没用到索引

USING TEMPORARY：用到了临时表，groupBy的时候会出现

Using index：排序字段是索引

# 优化工具

## profiles

```sql
#显示查询的sql
SHOW profiles;
#显示某次查询的过程
SHOW profile ALL  FOR QUERY 157 
```



