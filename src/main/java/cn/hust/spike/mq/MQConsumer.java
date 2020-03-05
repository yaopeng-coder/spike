package cn.hust.spike.mq;

import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.util.JsonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-04 16:29
 **/
@Component
public class MQConsumer {

    private DefaultMQPushConsumer consumer;

    @Autowired
    ProductStockMapper productStockMapper;


    @Value("${mq.nameserver.addr}")
    private String nameServerAddr;

    @Value("${mq.topicname}")
    private String topicName;




    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stockConsumerGroup");
        consumer.setNamesrvAddr(nameServerAddr);

        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        //消费这个主题下所有的消息
        consumer.subscribe(topicName,"*");

        //注册事件监听 当主题有消息时如何消费
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

                //实现库存真正到数据库内扣减的逻辑
                Message message = msgs.get(0);
                String jsonString  = new String(message.getBody());

                Map<String, Integer> map = JsonUtil.string2Obj(jsonString, new TypeReference<HashMap<String, Integer>>() {
                });
                Integer productId = map.get("productId");
                Integer amount = map.get("amount");

                productStockMapper.decreaseStock(productId,amount);


                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });


        consumer.start();

    }


}
