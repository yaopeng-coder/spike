package cn.hust.spike.mq;

import cn.hust.spike.common.RedisLock;
import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.service.IOrderService;
import cn.hust.spike.service.IProductSevice;
import cn.hust.spike.service.IPromoService;
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
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    IOrderService orderService;

    @Autowired
    IPromoService promoService;

    @Autowired
    IProductSevice productSevice;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisLock redisLock;



    @Value("${mq.nameserver.addr}")
    private String nameServerAddr;

    @Value("${mq.topicname}")
    private String topicName;

    private static final int TIMEOUT = 10 * 1000; //超时时间 10s




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

                //做好幂等性设计 ，注意即使我们两个后台服务器都运行，但只会有一个消费到这条消息，因为同属于一个消费组，和之前的定时关闭订单不同
                //这里的幂等性是为了防止消息被同一个服务器重复消费,例如消费最后的确认网络丢失，broker就会重传消息


                //1.实现库存真正到数据库内扣减的逻辑
                Message message = msgs.get(0);
                String jsonString  = new String(message.getBody());

                Map<String, Object> map = JsonUtil.string2Obj(jsonString, new TypeReference<HashMap<String, Object>>() {
                });

                String messageId = (String)map.get("messageId");

                long time = System.currentTimeMillis() + TIMEOUT;

                if(redisLock.lock(messageId+ "Lock",String.valueOf(time))){
                    //获取分布式锁成功 需要去redis查看当前消息的消费状态
                    if(redisTemplate.opsForValue().get(messageId + "Status").equals("false")){
                        //说明未消费  执行消费逻辑

                        Integer productId = (Integer)map.get("productId");
                        Integer amount = (Integer)map.get("amount");


                        productStockMapper.decreaseStock(productId,amount);


                        //2.增加产品销量   注意增加产品销量其实也是存在行锁
                        productSevice.increaseSales(productId,amount);
                        redisTemplate.opsForValue().set(messageId + "Status","true");

                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                //获取分布式锁失败消息重试
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;




            }
        });


        consumer.start();

    }




}
