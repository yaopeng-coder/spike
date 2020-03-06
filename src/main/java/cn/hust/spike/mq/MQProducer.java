package cn.hust.spike.mq;

import cn.hust.spike.common.Const;
import cn.hust.spike.dao.StockLogMapper;
import cn.hust.spike.entity.StockLog;
import cn.hust.spike.exception.BusinessException;
import cn.hust.spike.service.IOrderService;
import cn.hust.spike.util.JsonUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-04 16:08
 **/
@Component
public class MQProducer {

    private DefaultMQProducer producer;

    private TransactionMQProducer transactionMQProducer;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private StockLogMapper stockLogMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${mq.nameserver.addr}")
    private String nameServerAddr;

    @Value("${mq.topicname}")
    private String topicName;


    //如果想在生成对象时候完成某些初始化操作，
    // 而偏偏这些初始化操作又依赖于依赖注入，那么就无法在构造函数中实现，可以使用@PostConstruct注解一个方法来完成初始化
    @PostConstruct
    public void init() throws MQClientException {

        producer = new DefaultMQProducer("producerGroup");
        producer.setNamesrvAddr(nameServerAddr);
        producer.setSendMsgTimeout(60000);
        producer.start();

        transactionMQProducer = new TransactionMQProducer("transaction_mq_producer");
        transactionMQProducer.setNamesrvAddr(nameServerAddr);
        transactionMQProducer.setSendMsgTimeout(60000);

        transactionMQProducer.setTransactionListener(new TransactionListener() {

            //在这里执行本地事务的逻辑
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                Integer productId = (Integer) ((Map) arg).get("productId");
                Integer userId = (Integer) ((Map) arg).get("userId");
                Integer promoId = (Integer) ((Map) arg).get("promoId");
                Integer amount = (Integer) ((Map) arg).get("amount");
                String stockLogId = (String)((Map) arg).get("stockLogId");

                try {
                    orderService.createOrder(userId,productId,amount,promoId,stockLogId);
                } catch (BusinessException e) {
                    //说明事务要回滚
                    e.printStackTrace();
                    //更新库存流水状态
                    StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
                    stockLog.setStatus(Const.Stock_Status.STOCK_ROLLBACK.getCode());
                    stockLogMapper.updateByPrimaryKeySelective(stockLog);

                    return LocalTransactionState.ROLLBACK_MESSAGE;

                }


                return LocalTransactionState.COMMIT_MESSAGE;
            }

            /**
             * 在提交或者回滚事务消息时发生网络异常，RocketMQ 的 Broker 没有收到提交或者回滚的请求，
             * Broker 会定期去 Producer 上反查这个事务对应的本地事务的状态，然后根据反查结果决定提交或者回滚这个事务
             * @param msg
             * @return
             */
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {

                String jsonString  = new String(msg.getBody());

                Map<String, Object> map = JsonUtil.string2Obj(jsonString, new TypeReference<HashMap<String, Object>>() {
                });
                String stockLogID = (String) map.get("stockLogId");

                //根据库存流水ID 去本地数据库中反查本地事务情况
                StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogID);
                if(stockLog == null || stockLog.getStatus() == Const.Stock_Status.INIT_STOCK_STATUS.getCode()){
                    return LocalTransactionState.UNKNOW; //Broker 过一会再来查
                }
                if(stockLog.getStatus() == Const.Stock_Status.STOCK_DECREASE_SUCCESS.getCode()){
                    return LocalTransactionState.COMMIT_MESSAGE;
                }else {
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

            }
        });
        transactionMQProducer.start();
    }




    /**
     * 事务型同步库存扣减消息
     * @param productId
     * @param amount
     * @return
     */
    public boolean transactionAsyncReduceStock(Integer userId,Integer productId,Integer amount,Integer promoId,String stockLogId){
        Map<String,Object> bodyMap = new HashMap<>();

        bodyMap.put("productId",productId);
        bodyMap.put("amount",amount);
        bodyMap.put("stockLogId",stockLogId);
        bodyMap.put("userId",userId);
        bodyMap.put("promoId",productId);

        //消息的分布式ID 用来做好幂等性设计

        String messageId = orderService.generateId();
        bodyMap.put("messageId",messageId);
        redisTemplate.opsForValue().set(messageId + "Status","false");

        Map<String,Object> argsMap = new HashMap<>();

        argsMap.put("userId",userId);
        argsMap.put("productId",productId);
        argsMap.put("amount",amount);
        argsMap.put("promoId",productId);
        argsMap.put("stockLogId",stockLogId);


        Message message = new Message(topicName,JsonUtil.obj2String(bodyMap).getBytes(Charset.forName("UTF-8")));
        TransactionSendResult sendResult = null;
        try {
             sendResult = transactionMQProducer.sendMessageInTransaction(message, argsMap);

        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if(sendResult.getLocalTransactionState() == LocalTransactionState.COMMIT_MESSAGE){
            return true;
        }else {
            return false;
        }

    }




    /**
     * 同步库存扣减消息 根据返回值判断消息是否发送成功
     * @param productId
     * @param amount
     * @return
     */
    public boolean asyncStockReduceMessage(Integer productId, Integer amount){
        Map<String,Integer> map = new HashMap<>();

        map.put("productId",productId);
        map.put("amount",amount);

        Message message = new Message(topicName,"reduceStock", JsonUtil.obj2String(map).getBytes(Charset.forName("UTF-8")));

        try {
            producer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }


}
