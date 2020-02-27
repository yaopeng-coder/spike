package cn.hust.spike.service.impl;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.converter.Product2ProductDTO;
import cn.hust.spike.converter.ProductDTO2Product;
import cn.hust.spike.dao.ProductMapper;
import cn.hust.spike.dao.ProductStockMapper;
import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.dto.PromoDTO;
import cn.hust.spike.entity.Product;
import cn.hust.spike.entity.ProductStock;
import cn.hust.spike.service.IProductSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:22
 **/
@Service
public class ProductService implements IProductSevice{

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductStockMapper productStockMapper;

    @Autowired
    private PromoService promoService;


    /**
     * 创建产品
     * @param productDTO
     * @return
     */
    @Transactional
    @Override
    public ServerResponse createProduct(ProductDTO productDTO){

        //插入商品到数据库
        Product product = ProductDTO2Product.conver(productDTO);
        productMapper.insertSelective(product);

        //插入商品库存到数据库
        ProductStock productStock = new ProductStock();
        productStock.setProductId(product.getId());
        productStock.setStock(productDTO.getStock());
        productStockMapper.insertSelective(productStock);

        return ServerResponse.createBySuccess();

    }


    /**
     * 查询产品详情
     * @param id
     * @return
     */
    public ServerResponse<ProductDTO> productDetail(Integer id){

        Product product = productMapper.selectByPrimaryKey(id);
        if(product == null){
            return ServerResponse.createByErrorMessage("该商品不存在");
        }

        //查看对应商品是否是秒杀商品
        PromoDTO promoDTO = promoService.getPromoByProductId(id);


        ProductStock productStock = productStockMapper.selectByProductId(product.getId());

        ProductDTO productDTO = Product2ProductDTO.conver(product,productStock,promoDTO);
        productDTO.setId(id);

        return ServerResponse.createBySuccess(productDTO);


    }

    /**
     * 商品列表
     * @return
     */
    public ServerResponse productList(){

        List<Product> products = productMapper.selectProductList();

        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductStock productStock = productStockMapper.selectByProductId(product.getId());
            ProductDTO productDTO = Product2ProductDTO.conver(product, productStock,null);
            return productDTO;
        }).collect(Collectors.toList());

        return ServerResponse.createBySuccess(productDTOList);

    }
}
