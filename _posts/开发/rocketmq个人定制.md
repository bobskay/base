---
title: rocketmq个人定制
date: 2018-08-15 11:15:56
categories: []
tags: [rocketmq]
typora-copy-images-to: ../images
typora-root-url:  E:/blog
---

由于网络和性能因素，rocketmq对于重复消费，顺序消费，以及事务消息的解决方案，就是不解决。

客户端根据自身业务情况，需要在一致性和可用性之间做出取舍，自己给出方案

<!--more-->

我的方案是分别在发送方和消费方创建2张表：发送表和消费表，通过本地事务的方式解决。同时对于每个topic，新增一个结果确认的消息，发送方发送消息同时，需要消费这个结果确认的topic。大体流程

1. 发送方往发送表插入记录，并生成sendid
2. 发送方执行业务代码，如果成功就往MQ发送消息，如果失败则回滚事务
3. 消费方收到消息后，往消费表插入记录，由于sendId做了唯一约束，如果插入失败，表明已经消费过了
4. 插入成功后执行业务代码
5. 业务本身的成功和失败，直接更新消费表
6. 如果出现异常，回滚事务，相当于没消费
7. 消费成功往MQ发送结果
8. 发送方收到消费结果，更新发送表，本次消费结束

# 发送

## 流程图

```flow
st=>start: 准备发送
beginTx=>subroutine: 开启事务
preSend=>operation: 插入发送表
preSendResult=>condition: 插入结果
sendFail=>end: 结束
sendBus=>operation: 业务代码
sendBusResult=>condition: 执行结果
commitTx=>subroutine: 提交事务
rollbackTx=>subroutine: 回滚事务
sendMessage=>end: 发送消到MQ

st->beginTx->preSend->preSendResult
preSendResult(no)->rollbackTx->sendFail
preSendResult(yes)->sendBus->sendBusResult
sendBusResult(yes)->commitTx->sendMessage
sendBusResult(no)->rollbackTx->sendFail

```

## 异常点

由于执行的是本地事务，所以只要消息表里有记录，就说明业务成功了，需要发送消。如果没记录，就说明不需要发。唯一异常原因是消息没发送到MQ，这个问题可以等到消息确认环节再解决。

# 接收

## 流程图

```flow
st=>start: 收到消息
beginTx=>subroutine: 开始事务
insertConsumer=>operation: 插入消费表
insertReslut=>condition: 插入结果
ignore=>end: 重复消息,忽略
consumerBus=>operation: 业务代码
consumerResult=>condition: 执行成功
commitTx=>subroutine: 提交事务
rollTx=>subroutine: 回滚事务
consumerFail=>end: 系统异常,当做未消费处理
consumerSuccess=>operation: 消费成功,记录业务状态
sendResult=>operation: 向MQ发送消费结果
st->beginTx->insertConsumer->insertReslut
insertReslut(no)->ignore
insertReslut(yes)->consumerBus->consumerResult
consumerResult(yes)->commitTx->consumerSuccess->sendResult
consumerResult(no)->rollTx->consumerFail

```

## 异常点

业务自身结果，例如余额不足，库存不足等情况，属于业务状态，这种情况当做消费成功处理，只需要把结果通知到生产方就好了。只有出现了未知 错误的时候，消息表和业务一起回滚，等待消息重发。

非业务的因素的异常，就是成功消费后，未成功向mq发送结果，这个情况和发送时的异常一样，可以等到确认环节处理

# 结果确认

## 流程图

```flow
st=>start: 收到消费结果
cond=>condition: 业务是否成功
success=>operation: 更新发送表
end=>end: 流程结束
rollback=>subroutine: 执行反向业务,更新发送表
st->cond
cond(yes)->success->end
cond(no)->rollback->end


```

## 异常点

这个地方对于MQ来说不存在异常的的情况

# 补偿机制

综上所述，对于MQ来说，所有因网络因素造成的消息未成功到达的情况，最终汇总后的结果都是发送表里的数据长期没有消费记录

所以要做的就是查询发送表里的超过一定时间，无消费结果的数据，向消费方发起查询。在主动查询的的情况下，只要不是服务器全部宕机，总能获取结果的，根据查询结果

- 如果是消费方未收到消息，就重发
- 如果消费方一直消费失败，发送预警，人工介入
- 已经有结果，直接更新记录

# 具体实现

## producer

- 在业务入口添加SendMessage注解
- 业务在需要发送某种消息的时候只需要调用msgService.send将数据插入消息表
- msgService.send除了往数据库插入数据外，还会将插入的记录放一份到当前线程
- 被SendMessage注解的方法结束，系统会从当前线程取出在此期间插入的消息，自动往mq发送消息

```java
@Transactional
@SendMessage
public OrderInfo insert(Long userId, Long bookId, Integer amount) {
    long orderId = newId();

    OrderCreateMsg orderMsg = new OrderCreateMsg();
    orderMsg.setBookId(bookId);
    orderMsg.setAmount(amount);
    orderMsg.setOrderId(orderId);
    Long orderSendId = msgService.send(orderMsg);//插入订单创建消息

    EmailMsg email = new EmailMsg();
    email.setContent("购买图书:" + bookId);
    email.setBusId(orderId + "");
    email.setSubject("准备购买图书");
    email.setToAddress("xxxxx@xxxx.com");
    Long emailMsId = msgService.send(email);//插入邮件消息

    OrderInfo order = new OrderInfo();
    order.setBookMsId(orderSendId);
    order.setEmailMsId(emailMsId);
    order.setCreateTime(new Date());
    order.setOrderId(orderId);
    order.setUserId(userId);
    order.setBookId(bookId);
    order.setAmount(amount);
    orderDao.insert(order);
    return order;
}
```

系统在创建一个topic的时候，会自动创建一个叫%RESULT%XXX的topic，这个功能在我自己写的rocketmq管理界面里实现了

![1534313529114](/blog/images/1534313592698.png)

produer决定往某个topic发送消息的时候，会通过push的方式消费这个topic对应的%RESULT%topic。

## consumer

消费者在监听到消息后，进入主业务方法

- 在这个方法上标注ConsumerMessage
- 业务方通过msgService.prepare判断消费是否重复消费
- 消费结束后通过msgService.consumer更新消费结果
- 系统在ConsumerMessage标记的方法结束后，会从当前线程取出消费过的消息，往MQ里发送消费结果

```java
@ConsumerMessage
@Transactional
public void sendEmail(MessageExt msg) throws Exception {
    log.debug("收到邮件消息:" + jsonUtil.toString(msg));
    EmailMsg email = msgService.prepare(msg, EmailMsg.class, true);//锁定
    if (email == null) {
        return;
    }

    ConsumerResult result = ConsumerResult.UNKNOWN;
    try {
        result = doBussiness(email);
    } finally {
        switch (result) {
            case SUCCESS:
            case ROLLBACK:
                msgService.consumer(msg, email, result, true);//确认
                break;
            case UNKNOWN:
                throw new RuntimeException("回滚事务,当做未消费处理");
            default:
                break;
        }
    }
}

```

## 定时任务

produer需启动一个定时任务，定期查询超过一定时间，还无消费结果记录。

```sql
SELECT * FROM m_msgsend WHERE createTime<TIMESTAMPADD(MINUTE,-10,NOW()) AND msgConsumerId IS NULL
```

consumer端启动的时候，会自动启动一个服务，用于监听那些来查询消费结果的请求，这个用rocketmq封装的org.apache.rocketmq.remoting.netty.NettyRemotingServer实现

```java
private NettyRemotingServer server;
public MsgConfirmServer(int threadNum,String host,int port,MsgService msgService,KvConfigService kvConfigService) {
    this.executorService =
        Executors.newFixedThreadPool(threadNum, new ThreadFactoryImpl("MsgConfirmServer_"));
    NettyServerConfig config=new NettyServerConfig();
    config.setListenPort(port);
    server=new NettyRemotingServer(config);
    //设置请求执行类
    server.registerDefaultProcessor(new MsgConfirmProcessor(msgService), executorService);
    this.kvConfigService=kvConfigService;
    this.address=host+":"+port;
}
```

MsgConfirmProcessor里的实现，就是简单的通过sendId查找消费结果

```java
public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
    MsgConsumerHeader requestHeader = (MsgConsumerHeader) request.decodeCommandCustomHeader(MsgConsumerHeader.class);

    //查找通过发送id查找消费结果,如果没有记录说明还没被消费
    MsgConsumer consumer=msgService.getBySendId(requestHeader.getSendId());
    MsgConfirm confirm=new MsgConfirm();
    if(consumer!=null) {
        confirm.setBusState(consumer.getBusState());
        confirm.setMsgConsumerId(consumer.getMsgConsumerId());
    }
    //封装结果
    final RemotingCommand response = RemotingCommand.createResponseCommand(null);
    byte[] content = confirm.encode();
    response.setBody(content);
    response.setCode(ResponseCode.SUCCESS);
    response.setRemark(null);
    return response;
}
```

封装请求的MsgConsumerHeader对象，里面只需要一个sendId参数

```java
@Data
public class MsgConsumerHeader  implements CommandCustomHeader{
	//消息发送id
	private Long sendId;
	
	@Override
	public void checkFields() throws RemotingCommandException {
	}
}
```

# 顺序消费

通过以上的方案解决了重复消费，和最终一致性的问题。至于顺序消费，不太严格的情况下，只要将消息按key做分类，MessageQueue本身是有顺序的，通常请求下都能保证

```java
// 根据key做hash保证相同的key进入相同的messagQueue
public static class QueueSelector implements MessageQueueSelector {
    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
        String s = arg + "";
        int flag = Math.abs(s.hashCode());
        int no = flag % mqs.size();
        return mqs.get(no);
    }
}
```

比较严格的情况下，将tag作为顺序的标识，同一业务下，每次插入消息发送表的时候，通过业务id查询最大的tag和当前的tag比较，如果tag+1和当前的不符的话，说明还有消息未到达，需等会再消费

```sql
SELECT MAX(tag) FROM m_msgsend WHERE busid='xxxxx';
```

