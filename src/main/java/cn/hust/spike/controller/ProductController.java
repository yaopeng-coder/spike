package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dto.ProductDTO;
import cn.hust.spike.service.impl.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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



    @PostMapping(value = "/createProduct",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse createProduct(@Valid ProductDTO productDTO,
                                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("【注册参数】参数不正确, productDTO={}", productDTO);
            return ServerResponse.createByErrorMessage("注册参数不正确");
        }

        return productService.createProduct(productDTO);

    }

    @RequestMapping(value = "/detail")
    public ServerResponse<ProductDTO> productDetail(@RequestParam(name = "id") Integer id){

        return productService.productDetail(id);
    }


    @RequestMapping(value = "/list")
    public ServerResponse  productList(){

        return productService.productList();
    }




}
