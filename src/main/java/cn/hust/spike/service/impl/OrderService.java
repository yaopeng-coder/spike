package cn.hust.spike.service.impl;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dao.*;
import cn.hust.spike.entity.OrderInfo;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.Sequence;
import cn.hust.spike.entity.User;
import cn.hust.spike.service.IOrderService;
import cn.hust.spike.util.BigDecimalUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 11:21
 **/
@Service
public class OrderService implements IOrderService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;


    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private SequenceMapper sequenceMapper;

    /**
     * 创建订单
     * @param userId
     * @param productId
     * @param amount
     * @return
     */
    @Transactional
    public ServerResponse createOrder(Integer userId,Integer productId,Integer amount){

        //1.检查下单信息是否正确
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("该用户不存在");
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("该商品不存在");
        }

        if(amount <= 0 || amount >99){
            return ServerResponse.createByErrorMessage("下单数量不正确");
        }

        //2.落单减库存

        int affectRow = productStockMapper.decreaseStock(productId, amount);
        if(affectRow <=0){
            return ServerResponse.createByErrorMessage("库存不足");
        }

        //3.订单入库
        OrderInfo order = new OrderInfo();

          //生成交易流水号
        order.setId(generateId());

        order.setUserId(userId);
        order.setProductId(productId);
        order.setProductPrice(product.getPrice().doubleValue());
        order.setAmount(amount);
        order.setPromoId(0);
        order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),product.getPrice().doubleValue()).doubleValue());

        orderInfoMapper.insert(order);

        //增加产品销量
        productMapper.increaseSales(productId,amount);

        //4.返回前端

        return ServerResponse.createBySuccess();


    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateId(){


        //交易流水号共16位，前8位是时间信息

        StringBuilder stringBuilder = new StringBuilder();

        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);

        //中间6位是自增序列
        Sequence sequence = sequenceMapper.selectByPrimaryKey("order_info");
        Integer currentValue = sequence.getCurrentValue();

        sequence.setCurrentValue(sequence.getCurrentValue() + sequence.getStep());
        sequenceMapper.updateByPrimaryKeySelective(sequence);
        String value = String.valueOf(currentValue);
        for( int i = 0; i < 6 - value.length(); i++){
            stringBuilder.append("0");
        }

        stringBuilder.append(value);

        //最后两位是分库分表位，暂时为00
        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
