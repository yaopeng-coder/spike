package cn.hust.spike.controller;

import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.service.IPromoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-03 14:36
 **/
@RestController
@RequestMapping("/promo")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
@Slf4j
public class PromoController {

    @Autowired
    private IPromoService promoService;

    @RequestMapping(value = "/publish")
    public ServerResponse publishPromo(@RequestParam(name = "id") Integer id){
        promoService.publishPromo(id);
        return ServerResponse.createBySuccess();
    }
}
