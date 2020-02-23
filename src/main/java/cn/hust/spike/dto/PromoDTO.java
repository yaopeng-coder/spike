package cn.hust.spike.dto;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-23 16:05
 **/
@Data
public class PromoDTO {

    private Integer id;

    //秒杀活动状态 1表示还未开始，2表示进行中，3表示已结束
    private Integer status;

    //秒杀活动名称
    private String promoName;

    //秒杀活动的开始时间
    private DateTime startDate;

    //秒杀活动的结束时间
    private DateTime endDate;

    //秒杀活动的适用商品
    private Integer productId;

    //秒杀活动的商品价格
    private BigDecimal promoProductPrice;

}
