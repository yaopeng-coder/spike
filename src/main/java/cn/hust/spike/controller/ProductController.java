package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.Common.TokenCache;
import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.service.IPromoService;
import cn.hust.spike.service.impl.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeUnit;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-22 14:13
 **/
@RestController
@RequestMapping("/product")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
@Slf4j
public class ProductController {


    @Autowired
    private ProductService productService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IPromoService promoService;


    @PostMapping(value = "/createProduct",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse createProduct(@Valid ProductDTO productDTO,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【注册参数】参数不正确, productDTO={}", productDTO);
            return ServerResponse.createByErrorMessage("注册参数不正确");
        }

        return productService.createProduct(productDTO);

    }

    /**
     * //商品详情页浏览
     * @param id
     * @return
     */
    @RequestMapping(value = "/detail")
    public ServerResponse<ProductDTO> productDetail(@RequestParam(name = "id") Integer id){
        ProductDTO productDTO;
        Object nullValue = new Object();

        //1 .先取本地缓存
        productDTO = (ProductDTO) TokenCache.getKey("product_" + id);
        if(productDTO == null){
            try {
                // 2 .根据商品的id到redis内获取
                productDTO = (ProductDTO)redisTemplate.opsForValue().get("product_" + id);
            } catch (Exception e) {
               return ServerResponse.createByErrorMessage("该商品不存在");
            }

            // 3.若redis内不存在对应的itemModel,则访问下游service
            if(productDTO == null){
                ServerResponse<ProductDTO> serverResponse = productService.productDetail(id);
                productDTO = serverResponse.getData();
                if(productDTO == null){
                    //防止缓存穿透，设置回种空值，并且设置60s的过期时间
                    redisTemplate.opsForValue().set("product_" + id,nullValue);
                    redisTemplate.expire("product_" + id,60, TimeUnit.SECONDS);
                }else {
                    redisTemplate.opsForValue().set("product_" + id,productDTO);
                    redisTemplate.expire("product_" + id,3600, TimeUnit.SECONDS);
                }
            }
            if(productDTO != null){
                TokenCache.setKey("product_" + id,productDTO);
            }

        }



        return ServerResponse.createBySuccess(productDTO);

    }


    @RequestMapping(value = "/list")
    public ServerResponse  productList(){

        return productService.productList();
    }




}
