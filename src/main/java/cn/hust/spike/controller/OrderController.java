package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ResponseCode;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.entity.User;
import cn.hust.spike.service.impl.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        //1.用户点击详情页，若该商品是秒杀商品，会显示与秒杀商品相关的信息

        //2.在显示秒杀状态时，会


        //1.检查用户是否已经登陆
        User user = (User)request.getSession().getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆");
        }

        //2.创建订单

        return orderService.createOrder(user.getId(),productId,amount,promoId);


    }





}
