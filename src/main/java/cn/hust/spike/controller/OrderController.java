package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ResponseCode;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.entity.User;
import cn.hust.spike.service.impl.OrderService;
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

        //2.创建订单

        return orderService.createOrder(user.getId(),productId,amount,promoId);


    }





}
