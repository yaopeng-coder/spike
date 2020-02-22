package cn.hust.spike.service;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dto.ProductDTO;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:22
 **/
public interface IProductSevice {


    ServerResponse createProduct(ProductDTO productDTO);
    ServerResponse<ProductDTO> productDetail(Integer id);
}
