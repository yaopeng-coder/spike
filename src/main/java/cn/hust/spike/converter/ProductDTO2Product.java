package cn.hust.spike.converter;

import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.entity.Product;
import org.springframework.beans.BeanUtils;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:24
 **/
public class ProductDTO2Product {


    public static Product conver(ProductDTO productDTO){

        if(productDTO == null){
            return null;
        }

        Product product = new Product();
        BeanUtils.copyProperties(productDTO,product);
        return product;

    }
}
