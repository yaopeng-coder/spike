package cn.hust.spike.controller;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.service.impl.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * 创建订单
     * @param productId
     * @param amount
     * @param request
     * @return
     */
    @RequestMapping(value = "/createOrder" )
    public ServerResponse createOrder(@RequestParam(name = "productId") Integer productId, @RequestParam(name = "amount") Integer amount, HttpServletRequest request) {

        //1.检查用户是否已经登陆
//        User user = (User)request.getSession().getAttribute(Const.CURRENT_USER);
//        if(user == null){
//            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆");
//        }

        //2.创建订单

        return orderService.createOrder(1,productId,amount);


    }





}
