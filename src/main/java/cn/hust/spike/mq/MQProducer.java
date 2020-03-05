package cn.hust.spike.mq;

import cn.hust.spike.exception.BusinessException;
import cn.hust.spike.service.IOrderService;
import cn.hust.spike.util.JsonUtil;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

                try {
                    orderService.createOrder(userId,productId,amount,promoId);
                } catch (BusinessException e) {
                    //说明事务要回滚
                    e.printStackTrace();
                    return LocalTransactionState.ROLLBACK_MESSAGE;

                }


                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                return null;
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
    public boolean transactionAsyncReduceStock(Integer userId,Integer productId,Integer amount,Integer promoId){
        Map<String,Integer> bodyMap = new HashMap<>();

        bodyMap.put("productId",productId);
        bodyMap.put("amount",amount);

        Map<String,Integer> argsMap = new HashMap<>();

        argsMap.put("userId",userId);
        argsMap.put("productId",productId);
        argsMap.put("amount",amount);
        argsMap.put("promoId",productId);


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
