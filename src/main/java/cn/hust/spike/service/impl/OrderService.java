package cn.hust.spike.service.impl;

import cn.hust.spike.Common.ResponseCode;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dao.*;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.OrderInfo;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.Sequence;
import cn.hust.spike.entity.User;
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

    /**
     * 创建订单
     * @param userId
     * @param productId
     * @param amount
     * @return
     */
    @Transactional
    public ServerResponse createOrder(Integer userId,Integer productId,Integer amount,Integer promoId){

        //交易链路共有五次数据库操作 ，并且更新库存有行锁

        //1.检查用户信息是否异常

         //判断用户是为了进行用户风险控制（用户风控模型），例如判断用户是否异常异地登陆等等，所以实际情况中不仅仅时判断是否为空
       // User user = userMapper.selectByPrimaryKey(userId);

        //将用户风控模型从redis中查出  注意这里和controller存入的user作用不同，这里是来进行风控设置
        User user = userService.selectUserCacheById(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("该用户不存在");
        }

        //检查活动信息是否异常  引入redis ,若要紧急下线，直接清除redis对应的Key即可
      //  Product product = productMapper.selectByPrimaryKey(productId);

        //从redis中查询
        Product product = productSevice.selectProductCacheById(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("该商品不存在");
        }

        if(amount <= 0 || amount >99){
            return ServerResponse.createByErrorMessage("下单数量不正确");
        }

        PromoDTO promoDTO = null;
        if(promoId != null){
          // promoDTO = promoService.getPromoByProductId(productId);

            //从redis中查询
            promoDTO = promoService.selectPromoCacheByProductId(productId);
            if(promoDTO.getProductId() != productId){
                return ServerResponse.createByErrorMessage("活动信息不正确");
            }else if(promoDTO.getStatus()!= 2){
                return ServerResponse.createByErrorMessage("活动尚未开始");
            }
        }


        //2.落单减库存，注意 productId必须是有索引的，否则会导致锁表
//        int affectRow = productStockMapper.decreaseStock(productId, amount);
//        if(affectRow <=0){
//            //return ServerResponse.createByErrorMessage("库存不足");
//            //抛异常回滚，回滚数据库里的数据
//            throw new BusinessException(ResponseCode.NO_STOCK.getCode(),"库存不足");
//        }



        //更改减库存逻辑 将库存缓存化
        long result = redisTemplate.opsForValue().increment("promo_product_stock"+ productId,amount.intValue() * -1);

        //result代表剩下的库存  注意这里库存不足会回滚 但是回滚不了redis中数据，宁可少买的原则，即redis里数据可以比实际的少
        if(result < 0){
            throw new BusinessException(ResponseCode.NO_STOCK.getCode(),"库存不足");
        }


        //3.订单入库
        OrderInfo order = new OrderInfo();

          //生成交易流水号
        order.setId(generateId());

        order.setUserId(userId);
        order.setProductId(productId);

        if(promoId == null){
            order.setProductPrice(product.getPrice().doubleValue());
            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),product.getPrice().doubleValue()).doubleValue());
        }else {
            order.setProductPrice(promoDTO.getPromoProductPrice().doubleValue());
            order.setOrderPrice(BigDecimalUtil.mul(amount.doubleValue(),promoDTO.getPromoProductPrice().doubleValue()).doubleValue());
        }

        order.setAmount(amount);
        order.setPromoId(0);


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
