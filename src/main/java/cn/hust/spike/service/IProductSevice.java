package cn.hust.spike.service;

import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.entity.Product;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:22
 **/
public interface IProductSevice {


    ServerResponse createProduct(ProductDTO productDTO);
    ServerResponse<ProductDTO> productDetail(Integer id);
    Product selectProductCacheById(Integer productId);
    boolean decreaseStock(Integer productId , Integer amount );
    void  increaseStock(Integer productId , Integer amount );
    boolean asyncStockReduceMessage(Integer productId,Integer amount);

    void increaseSales(Integer productId,Integer amount)  ;
}
