package cn.hust.spike.service;

import cn.hust.spike.dto.PromoDTO;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 16:02
 **/
public interface IPromoService {
    PromoDTO getPromoByProductId(Integer productId);
}
