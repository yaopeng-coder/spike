package cn.hust.spike.controller;

import cn.hust.spike.common.Const;
import cn.hust.spike.common.ResponseCode;
import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.entity.User;
import cn.hust.spike.mq.MQProducer;
import cn.hust.spike.service.impl.OrderService;
import cn.hust.spike.service.impl.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 11:13
 **/
@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
@Slf4j
public class OrderController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private ProductService productService;


    /**
     *
     * 创建订单
     * @param productId
     * @param amount
     * @param request
     * @return
     */
    @RequestMapping(value = "/createOrder" ,method = {RequestMethod.POST},consumes = {Const.CONTENT_TYPE_FORMED})
    public ServerResponse createOrder(@RequestParam(name = "productId") Integer productId, @RequestParam(name = "amount") Integer amount, @RequestParam(name = "promoId",required = false) Integer promoId,HttpServletRequest request) {

        //整体优化分析
        //1.优化了校验参数 从redis中获取商品和用户 促销的信息
        //2.扣库存会导致数据库行锁，所以并发很慢，增加销量也会增加行锁，所以想办法将这两个异步化
        //3.数据库扣库存优化成扣redis缓存，但是为了保证数据库和redis数据的最终一致性，需要进行分布式事务处理
        //4.为了便于事务反查机制 增加了库存流水状态，并且增加了售罄标志，防止大量的流水记录
        //5.为了不重复消费，用分布式锁和使用redis存储消息id的方式保证了幂等性



        //促销整体逻辑
        //1.用户登陆后显示商品列表页
        //1.用户点击商品列表某个商品，进入详情页，若该商品是秒杀商品，会显示与秒杀商品相关的信息，注意状态字段是在DTO对象中，而不是在数据库中

        //2.在显示秒杀状态时，点击下单会将秒杀ID也传入，并且校验其状态
        // （注意状态是根据开始和结束时间计算，所以为了对此访问数据库设计状态字段，冗余到DTO对象中）



        String token = httpServletRequest.getParameterMap().get("token")[0];
        if(StringUtils.isEmpty(token)){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆");
        }
        //1.检查用户是否已经登陆
     //   User user = (User)request.getSession().getAttribute(Const.CURRENT_USER);

        User user = (User)redisTemplate.opsForValue().get(token);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆");
        }


        //2.下单前先查看库存售罄标识 ，若售罄可以直接返回，这样就不用记录大量的库存流水状态
        String stockInvalid = (String)redisTemplate.opsForValue().get("promo_stock_invalid_" + productId);
        if(stockInvalid != null && stockInvalid.equals("true")){
            return ServerResponse.createByErrorMessage("库存已售罄");
        }

        //3.先记录库存流水状态
        String stockLogId = productService.initStockStatus(productId, amount);



        //4.创建订单  减库存和创建订单，创建订单之所以不异步化，因为前端需要得到订单的信息才能返回，这样用户才能创建订单
        //所以即使这一步异步化给用户的体验也不是很好

        //return orderService.createOrder(user.getId(),productId,amount,promoId);
        boolean result = mqProducer.transactionAsyncReduceStock(user.getId(), productId, amount, promoId,stockLogId);
        if(result){
            return ServerResponse.createBySuccess("下单成功");
        }else {
            return ServerResponse.createByErrorMessage("库存不足");
        }


    }





}
