package cn.hust.spike.service.impl;

import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.dao.PromoMapper;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.ProductStock;
import cn.hust.spike.entity.Promo;
import cn.hust.spike.entity.User;
import cn.hust.spike.service.IProductSevice;
import cn.hust.spike.service.IPromoService;
import cn.hust.spike.service.IUserService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 16:02
 **/
@Service
public class PromoService implements IPromoService {


    @Autowired
    private PromoMapper promoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductSevice productSevice;

    public PromoDTO getPromoByProductId(Integer productId){

        Promo promo = promoMapper.selectByProductId(productId);
        if(promo == null){
            return null;
        }

        PromoDTO promoDTO = convert(promo);

        return promoDTO;

    }
    public PromoDTO convert(Promo promo){
        PromoDTO promoDTO = new PromoDTO();
        BeanUtils.copyProperties(promo,promoDTO);
        promoDTO.setStartDate(promo.getStartDate().toString());
        promoDTO.setEndDate(promo.getEndDate().toString());
        promoDTO.setPromoProductPrice(new BigDecimal(promo.getPromoProductPrice()));

        if(new DateTime(promo.getEndDate()).isBeforeNow()){
            promoDTO.setStatus(0); //已经结束
        }else if(new DateTime(promo.getStartDate()).isAfterNow()){
            promoDTO.setStatus(1); //还未开始
        }else {
            promoDTO.setStatus(2); //正在进行中
        }

        return promoDTO;
    }


    /**
     * 从redis中查询promo
     * @param productId
     * @return
     */
    public PromoDTO selectPromoCacheByProductId(Integer productId){
        PromoDTO promoDTO = (PromoDTO) redisTemplate.opsForValue().get("promoCache"+ productId);
        if(promoDTO == null){
            promoDTO = this.getPromoByProductId(productId);
            redisTemplate.opsForValue().set("promoCache"+ productId,promoDTO);
        }

        return promoDTO;

    }


    /**
     * 活动发布  可以用定时任务解决 定时将商品上架 将库存和令牌更新到redis中 同理 可以启动一个定时任务下架
     * @param promoId
     */
    public void publishPromo(Integer promoId){

        //首先判断对应的活动存不存在

        Promo promo = promoMapper.selectByPrimaryKey(promoId);
        if(promo == null || promo.getProductId() == null || promo.getProductId().intValue() == 0)
                return;

        //存在设置对应活动商品的库存进缓存 ,注意这里得到缓存然后再设置缓存 中间可能存在商品被人购买，更加合理的逻辑是设置商品上下价功能
        //在活动开始商品上架时得到库存，不上架无法购买，就可以解决

        ProductStock productStock = productStockMapper.selectByProductId(promo.getProductId());

        //存入库存
        redisTemplate.opsForValue().set("promo_product_stock" + promo.getProductId(),productStock.getStock());

        //存入令牌的数量  库存的5倍
        redisTemplate.opsForValue().set("promo_product_tokenCount"+promo.getProductId(),productStock.getStock() * 5);


    }


    /**
     * 生成秒杀令牌
     * @param userId
     * @param promoId
     * @param productId
     * @return
     */
    public String generateSeckillToken(Integer userId,Integer promoId,Integer productId){

        //判断秒杀库存售罄标识
        String stockInvalid = (String)redisTemplate.opsForValue().get("promo_stock_invalid_" + productId);
        if(stockInvalid != null && stockInvalid.equals("true")){
            return null;
        }

        Promo promo = promoMapper.selectByPrimaryKey(promoId);
        if(promo == null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        PromoDTO promoDTO = convert(promo);
        if(promoDTO.getProductId() != productId){
            return null;
        }else if(promoDTO.getStatus()!= 2){
           return null;
        }

        //判断产品是否存在
        Product product = productSevice.selectProductCacheById(productId);
        if(product == null){
            return null;
        }

        //判断用户是否存在
        User user = userService.selectUserCacheById(userId);
        if(user == null){
            return null;
        }


        //获取秒杀大闸的count数量
       Long result =  redisTemplate.opsForValue().increment("promo_product_tokenCount"+productId, - 1);
        if(result < 0){
            return null;
        }

        //生成token并且存入redis内并给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-","");

        redisTemplate.opsForValue().set("seckillToken-"+ promoId+"-"+ productId + "-"+userId,token);
        redisTemplate.expire("seckillToken-"+ promoId+"-"+productId + "-"+userId,5, TimeUnit.MINUTES);

        return token;


    }
}
