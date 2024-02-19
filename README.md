# myrpc
手写rpc项目

# 重要bug

1. 重复启动消费者出现超时错误

出现章节：3.4-3.6（3.6已修正）

现象：

![img](README.assets/1708333295406-eb35d05c-8edc-4df8-8fca-649b559f4277.png)

![img](README.assets/1708333302326-a1c293b7-c668-42d2-8d12-d50964d84af6.png)

![img](README.assets/1708333307583-7937506c-a5d1-40b7-92a3-8d8d751e5f48.png)



原因：

RpcConsumerHandler

![img](README.assets/1708333346233-cf9478c2-6dda-4fab-b628-86de516c37fe.png)
