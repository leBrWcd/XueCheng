# 学成在线

## 概述
本项目采用前后端分离架构，后端采用SpringBoot、SpringCloud技术栈开发，数据库使用了MySQL，
还使用的Redis、消息队列、分布式文件系统、Elasticsearch等中间件系统。
划分的微服务包括：内容管理服务、媒资管理服务、搜索服务、订单支付服务、 学习中心服务、系统管
理服务、认证授权服务、网关服务、注册中心服务、配置中心服务等。

![image](https://user-images.githubusercontent.com/83166781/216280159-0e880693-9693-4f4a-ae0f-e827ef829f7c.png)

## 技术架构
学成在线项目采用当前流行的前后端分离架构开发，由以下流程来构成：用户层、CDN内容分发和加
速、负载均衡、UI层、微服务层、数据层。
![image](https://user-images.githubusercontent.com/83166781/216280322-8ef85032-05f5-44e7-aff8-e95816f7f78a.png)


## 技术亮点
### 1.视频上传-断点续传
通常视频文件都比较大，所以对于媒资系统上传文件的需求要满足大文件的上传要求。http协议本身对上传文件大小没有限制，但是客户的网络环境质量、电脑硬件环境等参差不齐，如果一个大文件快上传完了网断了没有上传完成，需要客户重新上传，用户体验非常差，所以对于大文件上传的要求最基本的是断点续传。
什么是断点续传：
        引用百度百科：断点续传指的是在下载或上传时，将下载或上传任务（一个文件或一个压缩包）人为的划分为几个部分，每一个部分采用一个线程进行上传或下载，如果碰到网络故障，可以从已经上传或下载的部分开始继续上传下载未完成的部分，而没有必要从头开始上传下载，断点续传可以提高节省操作时间，提高用户体验性。
![image](https://user-images.githubusercontent.com/83166781/218940212-a758785e-d678-44c9-8212-a2c68269b04c.png)
流程如下：
1、前端上传前先把文件分成块
2、一块一块的上传，上传中断后重新上传，已上传的分块则不用再上传
3、各分块上传完成最后在服务端合并文件

### 2.视频解码-任务广播分片
1.任务调度中心广播作业分片。
2、执行器收到广播作业分片，从数据库读取待处理任务。
3、执行器根据任务内容从MinIO下载要处理的文件。
4、执行器启动多线程去处理任务。
5、任务处理完成，上传处理后的视频到MinIO。
6、将更新任务处理结果，如果视频处理完成除了更新任务处理结果以外还要将文件的访问地址更新至任务处理表及文件表中，最后将任务完成记录写入历史表。
![image](https://user-images.githubusercontent.com/83166781/218940165-9cfef6ee-075d-4804-b699-ef03832fe8c9.png)
