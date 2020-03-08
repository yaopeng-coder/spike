package cn.hust.spike.service.impl;

import cn.hust.spike.common.Const;
import cn.hust.spike.common.ResponseCode;
import cn.hust.spike.dao.*;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.*;
import cn.hust.spike.exception.BusinessException;
import cn.hust.spike.service.IOrderService;
import cn.hust.spike.service.IProductSevice;
import cn.hust.spike.service.IUserService;
import cn.hust.spike.util.BigDecimalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private PromoService promoService;

    @Autowired
    private IProductSevice productSevice;


    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private SequenceMapper sequenceMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StockLogMapper stockLogMapper;

    /**
     * 创建订单
     * @param userId
     * @param productId
     * @param amount
     * @return
     */
//    @Transactional
//    public ServerResponse createOrder(Integer userId,Integer productId,Integer amount,Integer promoId,String stockLogId)throws BusinessException{
//
//        //交易链路共有五次数据库操作 ，并且更新库存有行锁
//
//        //1.检查用户信息是否异常
//
//         //判断用户是为了进行用户风险控制（用户风控模型），例如判断用户是否异常异地登陆等等，所以实际情况中不仅仅时判断是否为空
//       // User user = userMapper.selectByPrimaryKey(userId);
//
//        //将用户风控模型从redis中查出  注意这里和controller存入的user作用不同，这里是来进行风控设置
//        User user = userService.selectUserCacheById(userId);
//        if(user == null){
//            return ServerResponse.createByErrorMessage("该用户不存在");
//
//        }
//
//
//
//        //检查活动信息是否异常  引入redis ,若要紧急下线，直接清除redis对应的Key即可
//      //  Product product = productMapper.selectByPrimaryKey(productId);
//
//        //从redis中查询
//        Product product = productSevice.selectProductCacheById(productId);
//        if(product == null){
//            return ServerResponse.createByErrorMessage("该商品不存在");
//        }
//
//        if(amount <= 0 || amount >99){
//            return ServerResponse.createByErrorMessage("下单数量不正确");
//        }
//
//        PromoDTO promoDTO = null;
//        if(promoId != null){
//          // promoDTO = promoService.getPromoByProductId(productId);
//
//            //从redis中查询
//            promoDTO = promoService.selectPromoCacheByProductId(productId);
//            if(promoDTO.getProductId() != productId){
//                return ServerResponse.createByErrorMessage("活动信息不正确");
//            }else if(promoDTO.getStatus()!= 2){
//                return ServerResponse.createByErrorMessage("活动尚未开始");
//            }
//        }
//
//
//        //2.落单减库存，注意 productId必须是有索引的，否则会导致锁
//
//
//        //更改减库存逻辑 将库存缓存化
//        boolean flag = productSevice.decreaseStock(productId, amount);
//
//        if(!flag){
//            throw new BusinessException(ResponseCode.NO_STOCK);
//        }
//
//
//        //3.订单入库
//        OrderInfo order = new OrderInfo();
//
//          //生成交易流水号
//        order.setId(generateId());
//
//        order.setUserId(userId);
//        order.setProductId(productId);
//
//        if(promoId == null){
//            order.setProductPrice(product.getPrice().doubleValue());
//            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),product.getPrice().doubleValue()).doubleValue());
//        }else {
//            order.setProductPrice(promoDTO.getPromoProductPrice().doubleValue());
//            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),promoDTO.getPromoProductPrice().doubleValue()).doubleValue());
//        }
//
//        order.setAmount(amount);
//        order.setPromoId(0);
//        orderInfoMapper.insert(order);
//
//
//
//        //4.返回前端
//
//        //第一种：假设在这里投递消息，也有可能事务在最后提交时失败回滚，但是消息已经投递出去了,所以也是不可行的
////        boolean mqResult = productSevice.asyncStockReduceMessage(productId, amount);
////        if(!mqResult){
////            //加回库存
////            productSevice.increaseStock(productId,amount);
////            throw new BusinessException(ResponseCode.MESSAGE_SEND_FAIL.getCode(),ResponseCode.MESSAGE_SEND_FAIL.getDesc());
////        }
//
//        //第二种：这种通过事务管理器在事务提交后发送消息，若消息发送失败，仅仅是扣回redis库存是不足够的，
//        // 因为刚刚的事务中还创建了订单加销量等等，所以你消息发送失败必须回滚刚刚的事务才行
//        //通过这些方法，你发现还是要解决分布式事务的问题
////        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
////
////            @Override
////            public void afterCommit() {
////                boolean mqResult = productSevice.asyncStockReduceMessage(productId, amount);
////                if(!mqResult){
////                         //加回库存
////            productSevice.increaseStock(productId,amount);
////            throw new BusinessException(ResponseCode.MESSAGE_SEND_FAIL.getCode(),ResponseCode.MESSAGE_SEND_FAIL.getDesc());
////        }
////            }
////
////
////        });
//
//
//        //更新库存流水状态
//        StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
//        stockLog.setStatus(Const.Stock_Status.STOCK_DECREASE_SUCCESS.getCode());
//        stockLogMapper.updateByPrimaryKeySelective(stockLog);
//
//        return ServerResponse.createBySuccess();
//
//
//    }







    /**
     * 创建订单
     * @param userId
     * @param productId
     * @param amount
     * @param stockLogId
     * @return
     * @throws BusinessException
     */
    @Transactional
    public void createOrder(Integer userId,Integer productId,Integer amount,Integer promoId,String stockLogId)throws BusinessException{
        //1.校验参数  检验 4个参数， 从redis中查询  将风控策略前置到秒杀令牌中

//        User user = userService.selectUserCacheById(userId);
//        if(user == null){
//            throw new BusinessException(ResponseCode.ERROR.getCode(),"该用户不存在");
//        }

        Product product = productSevice.selectProductCacheById(productId);
//        if(product == null){
//            throw new BusinessException(ResponseCode.ERROR.getCode(),"该商品不存在");
//        }

        if(amount <= 0 || amount >99){
            throw new BusinessException(ResponseCode.ERROR.getCode(),"下单数量不正确");
        }


        PromoDTO promoDTO = null;
 //       if(promoId != null){
            // promoDTO = promoService.getPromoByProductId(productId);

            //从redis中查询
   //         promoDTO = promoService.selectPromoCacheByProductId(productId);
//            if(promoDTO.getProductId() != productId){
//                throw new BusinessException(ResponseCode.ERROR.getCode(),"活动信息不正确");
//            }else if(promoDTO.getStatus()!= 2){
//                throw new BusinessException(ResponseCode.ERROR.getCode(),"活动尚未开始");
//            }
  //      }

        //2.从redis中减去库存
        boolean decreaseStockFlag = productSevice.decreaseStock(productId, amount);
        if(!decreaseStockFlag){
            throw new BusinessException(ResponseCode.NO_STOCK);
        }


        //3.创建订单  创建订单没必要异步化
        //订单入库
        OrderInfo order = new OrderInfo();

        //生成交易流水号
        order.setId(generateId());

        order.setUserId(userId);
        order.setProductId(productId);

        if(promoId == null){
            order.setProductPrice(product.getPrice().doubleValue());
            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),product.getPrice().doubleValue()).doubleValue());
        }else {

             promoDTO = promoService.selectPromoCacheByProductId(productId);
            order.setProductPrice(promoDTO.getPromoProductPrice().doubleValue());
            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),promoDTO.getPromoProductPrice().doubleValue()).doubleValue());
        }

        order.setAmount(amount);
        order.setPromoId(0);
        orderInfoMapper.insert(order);

        //4.更新库存流水状态

        StockLog stockLog = stockLogMapper.selectByPrimaryKey(stockLogId);
        stockLog.setStatus(Const.Stock_Status.STOCK_DECREASE_SUCCESS.getCode());
        int result = stockLogMapper.updateByPrimaryKeySelective(stockLog);
        if(result <=0){
            //表示更新失败
            productSevice.increaseStock(productId, amount);
            throw new BusinessException(ResponseCode.ERROR.getCode(),"库存更新错误");
        }


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
