---
title: hexo搭建个人博客
date: 2018-07-29 17:27:11
categories: [code,hexo]
tags: [hexo]
typora-copy-images-to: ../../images
typora-root-url:  E:/blog/
---

记录这个博客搭建过程，目前为止用到的功能如下，不断丰富中

1. [hexo](https://hexo.io/zh-cn/)生成代码
2. [next](http://theme-next.iissnan.com/)主题
3. [码云](https://gitee.com/)web服务
4. [leancloud](https://leancloud.cn/)记录阅读量



编译并提交

```shell
e:
cd E:\blog\public\blog
hexo g
git add .
git commit -am '备注'
git push
```



<!--more-->

# 准备工作

这些工作因为以前就做过了，这里就不专门写出来了，

1. 安装npm（Node Package Manager ），我的版本是5.6.0
2. 安装TortoiseGit，我的版本是TortoiseGit 2.6.0.0
3. 注册码云账号，并创建项目，这里定的项目名称是blog
4. 本地新建目录，我用的是：E:\blog，下面如果没特殊说明，所有命令均是在这个目录下执行的

下面正式开始

进入工作目录

```shell
E:
cd blog
```



![1532892250057](/blog/images/1532892250057.png)

# 安装hexo

```shell
npm install -g hexo
```

# 创建项目

```shell
hexo init
```

![1532877733213](/blog/images/1532877733213.png)

启动，如果不行就执行一下npm install

```shell
hexo s
```

![1532877925010](/blog/images/1532877925010.png)

打开浏览器,在地址栏输入输入：http://127.0.0.1:4000/ 就可以直接访问了

![1532878027383](/blog/images/1532878027383.png)

# 更换主题

个人选的是[NexT](https://github.com/theme-next/hexo-theme-next)

可以到网站上将其下载到主题目录E:\blog\themes，也可以直接用git命令

```shell
git clone https://github.com/theme-next/hexo-theme-next.git themes/next
```

修改项目根路径下的配置文件（e:/blog/_config.yml，后面简称站点文件）

将theme改为next

```yaml
# Extensions
## Plugins: https://hexo.io/plugins/
## Themes: https://hexo.io/themes/
theme: next
```

修改next的配置文件（E:/blog/themes/next/_config.yml，后面简称主题文件）

还可以进一步更换样式,目前可选的有下面这些，根据个人喜好，可自行选择

```yaml
# Schemes
#scheme: Muse
#scheme: Mist
scheme: Pisces
#scheme: Gemini
```

# 基础配置

## 标题

```yaml
title: 个人博客
```

## 语言

```yaml
language: zh-CN
```

语言是和这个目录下的文件对应的：E:\blog\themes\next\languages

![1532880622334](/blog/images/1532880622334.png)

## 添加分类和标签

### 新建页面

```shell
hexo new page categories
hexo new page tage
```

![1532880847798](/blog/images/1532880847798.png)

### 修改生成页面的内容

E:\blog\source\categories\index.md

```markdown
---
title: 分类
date: 2018-07-30 00:12:56
type: "categories"
---
```

E:\blog\source\tags\index.md

```markdown
---
title: 标签
date: 2018-07-30 00:13:55
type: "tags"
---
```

### 修改主题文件

根据需要选择要显示的页面，因为分类和标签对个人比较有用，就先开放这两个

```yaml
menu:
  home: / || home
  #about: /about/ || user
  tags: /tags/ || tags
  categories: /categories/ || th
  #archives: /archives/ || archive
  #schedule: /schedule/ || calendar
  #sitemap: /sitemap.xml || sitemap
  #commonweal: /404/ || heartbeat
```

# 开始撰写

写博客唯一要做的事就是在这个目录（E:\blog\source\_posts）下新建md文件

除了头部有一些特殊要求外，其余部分基本可以自由发挥

## 头部信息

文章标题和分类等信息需要按专门的格式写

```markdown
---
title: 文章标题
date: 2018-07-29 22:48:10
categories: [一级分类,二级分类]
tags: [标签1,标签2]
---
```

## 文章主体

通过more便签可以将文正分为简介和正文两部分，下面是一个简单示例

```shell
这里是内容简介，点击阅读全文可以看到更多内容
<!--more-->
# 这里是一级标题
## 测试画图
'``flow
st=>start: 开始
op=>operation: 操作
cond=>condition: 判断
en=>end: 结束
st->op
op->cond
cond(yes)->en
'``
## 测试二级标题
# 一级标题2
# 一级标题3
```



# 实际效果

## 首页

![1532882626864](/blog/images/1532882626864.png)

## 阅读全文

![1532883398241](/blog/images/1532883398241.png)

## 分类

![1532883459275](/blog/images/1532883459275.png)

## 标签

![1532883485461](/blog/images/1532883485461.png)

# 发布到网上

由于网速问题原因，我暂时未将项目发布到[github](https://github.com/)，而是用了[码云](https://gitee.com/)

码云上提供了一个免费的Gitee Pages服务，可以用于展示静态页面

![1532884043812](/blog/images/1532884043812.png)

只要在项目里点击服务，然后点击Gitee Pages，再启动就可以了

![1532883923533](/blog/images/1532883923533.png)

## 修改项目根路径

项目取名叫blog，所以最终发布到网上的网址为https://xixhaha.gitee.io/blog/

为了保证页面和和资源能正常访问，需要对路径做一些修改

## 修改网站文件

将root改为/blog

```yaml
root: /blog
```

## 下载项目到本地

我下载到了这个目录：E:/blog/public/blog

```shell
git clone https://gitee.com/xixhaha/blog.git public/blog
```

## 修改生成目录

因为这个目录E:/blog/public/blog将会是最终的发布目录，所以要将生成的静态页面都放这里

修改站点文件

```yaml
public_dir: public/blog
```

## 生成静态页面

```shell
hexo g
```

## 提交到网站

用的是TortoiseGit ，所以只需要在blog目录按右键选择git commit就可以了

![1532893106342](/blog/images/1532893106342.png)

### commit

填写备注，随便写点什么，然后点击All和Commit

![1532885534198](/blog/images/1532885753389.png)

### push

commit后会转到push界面，直接点push



![1532885611516](/blog/images/1532885611516.png)

换成命令就是

```shell
e:
cd E:\blog\public\blog
hexo g
git add .
git commit -am '备注'
git push
```



# 总结

基本上这样个人的博客就算搭建完毕了，以后要撰写博客只需要进行以下几个步骤

1. 在E:\blog\blog\_posts目录下新建md文件
2. 开始撰写文章
3. 执行命令 hexo g
4. git commit and git push

# 个性化配置

## 按分类生成路径

文章的链接地址默认是按日期生成的，这样当要链接到自己文章时不太方便，可以改为按分类生成

修改网站文件

```yaml
permalink: :category/:title/
```

## 排序规则

默认是按发布时间倒序的，这里改成了按更新时间

```yaml
index_generator:
  path: ''
  per_page: 10
  order_by: -updated
```

## 图片问题

因为我将图片全放到了E:\blog\source\images目录下，所以在外网访问时图片的路径是这个样子的

```shell
/blog/images/1532877733213.png 
```

为了保证撰写文章时图片也能正常显示所以需要做一些调整

将网站配置源文件目录改为blog

```shell
source_dir: blog
```

每次插入图片时将图片复制到E:\blog\blog\images

我用的markdown编辑器是[typroa](https://www.typora.io/)
需要在头部做一些配置，这样就保证能本地链接的地址和网站地址一致，同时在每次插入图片时自动复制到指定目录

最终每次创建md文件时，都需要这么一个文件头

```markdown
---
title: 
date: 
categories: []
tags: []
typora-copy-images-to: ../images
typora-root-url:  E:/blog/
---
```
写了一个文件（blog.py）专门干这事，每次要写博客的时候只要双击这个文件就可以了

```python
# -*- coding: UTF-8 -*-
import datetime
import subprocess
import threading
now = datetime.datetime.now()
fileName= now.strftime("%Y%m%d%H%M%S")
now = now.strftime("%Y-%m-%d %H:%M:%S")

head='''---
title: 
date: {}
categories: []
tags: []
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

<!--more-->
'''.format(now)

fileName='E:/blog/blog/_posts/'+fileName+'.md'
fileHandle = open (fileName, 'w' )
fileHandle.write (head)
fileHandle.close()
print("创建文件:"+fileName)
cmd="cmd /c D:/Typora/Typora.exe "+fileName

def action():
    subprocess.call(cmd,shell=True,stdout=subprocess.PIPE)
t=threading.Thread(target=action)
t.start()
```

# 插件

## markdown画图

原始的hexo是不支持作图的，需要安装插件，目前试了只有流程图能用

```shell
npm install --save hexo-filter-flowchart
```

其它类型的图，暂时只能通过截图方式。。。

## 站内搜索

我大部分时间拿博客当云笔记用，所以搜索功能必不可少，幸好有插件实现了站内搜索功能

### 安装插件

```shell
npm install hexo-generator-searchdb --save 
```

### 修改站点文件

```yaml
search:
  path: search.xml
  field: post
  format: html
  limit: 10000
```

### 修改主题文件

```yaml
local_search:
  enable: true
```

### 最终效果

![1532687467766](/blog/images/1532687467766.png)

## 阅读次数

next内部已经集成了leancloud，我们要做的只是开启这个功能

### 修改主题文件

将leancloud_visitors.enable设true，并配置app_id和app_key，这两个值需要到leancloud上获取

```yaml
leancloud_visitors:
  enable: true
  app_id: XXXXXXXXXXX
  app_key: XXXXXXXXXXXXXXX
```

### 注册并登录leancloud

略

### 创建应用

点击头像-->点击应用按钮-->创建新应用

![1532931914832](/blog/images/1532931914832.png)

输入应用名，点击创建

![1532932008703](/blog/images/1532932008703.png)

### 创建Class

选择刚才创建的应用后，点击云存储-->创建class-->输入class名称-->创建class

这里名称要写**Counter**

![1532932124339](/blog/images/1532932150226.png)

### 获取app_id和app_key

将设置-->应用Key下的App ID和App Key写到上面说的主题文件里面

![1532932378669](/blog/images/1532932378669.png)

### 发布应用

```shell
e:
cd E:\blog\public\blog
hexo g
git add .
git commit -am '新增显示阅读次数功能'
git push
```

### 最终效果

![1532932631263](/blog/images/1532932631263.png)

访问网页后点击Counter可以查看实际存储的数据的值，time就是保存的阅读次数，可以随意修改

![1532932768632](/blog/images/1532933159586.png)



## 评论



# 可能遇到问题

## 无法删除文件

某些情况下会发现.md文件已经删除了，但最终还是会生成html，这时候需要清一下缓存。但clean命令会将整个public目录删掉，git信息也没了，所以需要先备份一下，具体命令如下

```shell
cd E:\blog
ren E:\blog\public public1
hexo clean
ren E:\blog\public1 public
hexo g
```

![1532896295850](/blog/images/1532896295850.png)

## 404

代码刚发布立即访问，有可能遇到临时无法访问的情况。如果不是这个原因，确认一下代码是否提交了，

使用TortoiseGit，默认是不提交新增文件的，所以commit前记得点一下All

![1532933462735](/blog/images/1532933462735.png)

## Template render error

生成html页面的时候报错

![1532949407329](/blog/images/1532949407329.png)

可能原因是md文件里写了这样的代码

```properties
{#xxx}
```

解决办法，想我这样写在代码块里就好了