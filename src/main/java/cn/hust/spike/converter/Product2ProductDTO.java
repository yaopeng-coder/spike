package cn.hust.spike.converter;

import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.ProductStock;
import org.springframework.beans.BeanUtils;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 15:15
 **/
public class Product2ProductDTO {


    public static ProductDTO conver(Product product, ProductStock productStock){
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product,productDTO);
        productDTO.setStock(productStock.getStock());
        return productDTO;

    }




}
