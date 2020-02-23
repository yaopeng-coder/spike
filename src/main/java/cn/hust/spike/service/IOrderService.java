package cn.hust.spike.service;

import cn.hust.spike.Common.ServerResponse;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 11:21
 **/
public interface IOrderService {

    ServerResponse createOrder(Integer userId, Integer productId, Integer amount);
}
