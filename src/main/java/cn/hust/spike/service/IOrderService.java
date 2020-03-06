package cn.hust.spike.service;

import cn.hust.spike.exception.BusinessException;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 11:21
 **/
public interface IOrderService {

    void createOrder(Integer userId, Integer productId, Integer amount,Integer promoId,String stockLogId) throws BusinessException ;
    String generateId();
}
