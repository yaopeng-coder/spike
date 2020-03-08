package cn.hust.spike.controller;

import cn.hust.spike.common.Const;
import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.common.TokenCache;
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


        //注意，把商品存储在缓存中是可以的，因为秒杀商品的库存和开始时间和终止时间都是固定的，存储在缓存中也不用担心数据更新不及时的问题
        // 前端拿到数据在进行计算提示秒杀信息，顶多秒杀后库存开始变化，而缓存中的库存都是固定的，那么我们也可以从redis中把库存取出来
        //这样秒杀后库存也能即时更新了，注意redis的库存是在有个发布活动时才存进redis中，那么我们可以判断有没有这个库存，有就更新，没有就算了

        //1 .先取本地缓存
        //本地缓存不要设置太长时间 ，一是因为内存问题 二是本地缓存不像redis一样好清理，需要每台应用服务器感知到数据的变更，一般可以用广播型的
        //mq消息解决，推荐rocketmq的广播消息，使得订阅对于商品消息变更的所有应用服务器都有机会清理本地缓存
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
