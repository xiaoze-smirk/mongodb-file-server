# MongoDB File Server（基于 MongoDB 的文件服务器）

MongoDB File Server is a file server system based on MongoDB. MongoDB File Server is committed to the storage of small files, such as pictures in the blog, ordinary documents and so on.

It's using some very popular technology like:

* MongoDB 3.6.2
* Spring Boot 1.5.3.RELEASE
* Thymeleaf 3.0.3.RELEASE
* Thymeleaf Layout Dialect 2.2.0
* Embedded MongoDB 2.0.0
* Gradle 4.4.1

基于 MongoDB 的文件服务器。MongoDB File Server 致力于小型文件的存储，比如博客中图片、普通文档等。由于MongoDB 支持多种数据格式的存储，对于二进制的存储自然也是不话下，所以可以很方便的用于存储文件。由于  MongoDB 的 BSON 文档对于数据量大小的限制（每个文档不超过16M），所以本文件服务器主要针对的是小型文件的存储。对于大型文件的存储（比如超过16M），MongoDB 官方已经提供了成熟的产品  [GridFS](https://docs.mongodb.com/manual/core/gridfs/)，读者朋友可以自行了解。




## Features

* Easy to use.
* RESTful API.
* ...

## APIs

Here are useful APIs.

* GET  /files/{pageIndex}/{pageSize} : Paging query file list.(分页查询文件列表)
* GET  /files/{id} : Download file.(下载某个文件)
* GET  /view/{id} : View file online.(在线预览某个文件。比如，显示图片)
* POST /upload : Upload file.(上传文件)
* DELETE /{id} : Delete file.(删除文件)


## How to 

It's so easy to start up the MongoDB File Server with 4 steps.

### 1. Get source

```shell
$ git clone https://github.com/xiaoze-smirk/mongodb-file-server.git
```

### 2. Modification
Open the file **gradle -> wrapper -> gradle-wrapper.properties**,
find the following words:
```shell
distributionUrl=file\:/E:/gradle-4.4.1-all/gradle-4.4.1-all.zip
```
modify the URL and use your local URL，then
open the springboot file **application.properties**,
find the following words:
```shell
spring.data.mongodb.uri=mongodb://localhost:27017/xiaoze
```
modify the URI and use your local MongoDB URI，then

### 3. Run

```shell
$ gradlew bootRun
```
or you can click the script **start.cmd**


### 4.Browse the web site
you can visit the application at <http://localhost:8081>.

 
## 本项目说明

本项目是根据原作者的 [mongodb-file-server](https://github.com/769418917/mongodb-file-server)项目进行更改，添加了页面记录删除和分页功能，优化了界面，修复了上传空文件bug，时间显示格式化等等。你若想了解更多关于该项目的信息，请关注原作者[github](https://github.com/769418917/mongodb-file-server)网址。
