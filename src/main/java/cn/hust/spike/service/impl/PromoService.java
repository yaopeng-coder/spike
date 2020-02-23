package cn.hust.spike.service.impl;

import cn.hust.spike.dao.PromoMapper;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.Promo;
import cn.hust.spike.service.IPromoService;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    public PromoDTO getPromoByProductId(Integer productId){

        Promo promo = promoMapper.selectByProductId(productId);
        if(promo == null){
            return null;
        }

        PromoDTO promoDTO = convert(promo);

        if(promoDTO.getEndDate().isBeforeNow()){
            promoDTO.setStatus(0); //已经结束
        }else if(promoDTO.getStartDate().isAfterNow()){
            promoDTO.setStatus(1); //还未开始
        }else {
            promoDTO.setStatus(2); //正在进行中
        }

        return promoDTO;

    }
    public PromoDTO convert(Promo promo){
        PromoDTO promoDTO = new PromoDTO();
        BeanUtils.copyProperties(promo,promoDTO);
        promoDTO.setStartDate(new DateTime(promo.getStartDate()));
        promoDTO.setEndDate(new DateTime(promo.getEndDate()));
        promoDTO.setPromoProductPrice(new BigDecimal(promo.getPromoProductPrice()));

        return promoDTO;
    }
}
