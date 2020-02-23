package cn.hust.spike.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:13
 **/
@Data
public class ProductDTO {


    private Integer id;

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    @NotBlank(message = "产品描述不能为空")
    private String description;

    @NotBlank(message = "图片地址不能为空")
    private String imgUrl;

    //商品的销量
    private Integer sales;



    //记录商品是否在秒杀活动中，以及对应的状态0：表示没有秒杀活动，1表示秒杀活动待开始，2表示秒杀活动进行中
    private Integer promoStatus;

    //秒杀活动价格
    private BigDecimal promoPrice;

    //秒杀活动ID
    private Integer promoId;

    //秒杀活动开始时间
    private String startDate;


}
