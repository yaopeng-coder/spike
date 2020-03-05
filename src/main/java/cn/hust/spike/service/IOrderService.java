package cn.hust.spike.service;

import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.exception.BusinessException;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 11:21
 **/
public interface IOrderService {

    ServerResponse createOrder(Integer userId, Integer productId, Integer amount,Integer promoId) throws BusinessException;
}
