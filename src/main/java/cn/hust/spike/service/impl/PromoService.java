package cn.hust.spike.service.impl;

import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.dao.PromoMapper;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.ProductStock;
import cn.hust.spike.entity.Promo;
import cn.hust.spike.service.IPromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

    public PromoDTO getPromoByProductId(Integer productId){

        Promo promo = promoMapper.selectByProductId(productId);
        if(promo == null){
            return null;
        }

        PromoDTO promoDTO = convert(promo);

        if(new DateTime(promo.getEndDate()).isBeforeNow()){
            promoDTO.setStatus(0); //已经结束
        }else if(new DateTime(promo.getStartDate()).isAfterNow()){
            promoDTO.setStatus(1); //还未开始
        }else {
            promoDTO.setStatus(2); //正在进行中
        }

        return promoDTO;

    }
    public PromoDTO convert(Promo promo){
        PromoDTO promoDTO = new PromoDTO();
        BeanUtils.copyProperties(promo,promoDTO);
        promoDTO.setStartDate(promo.getStartDate().toString());
        promoDTO.setEndDate(promo.getEndDate().toString());
        promoDTO.setPromoProductPrice(new BigDecimal(promo.getPromoProductPrice()));

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
     * 活动发布
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

        redisTemplate.opsForValue().set("promo_product_stock" + promo.getProductId(),productStock.getStock());


    }
}
