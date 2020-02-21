package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.form.UserForm;
import cn.hust.spike.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Random;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 14:20
 **/
@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials="true", allowedHeaders = "*")
@Slf4j
public class UserController {


    @Autowired
    private IUserService userService;



    /**
     * 注册用户
     * @param userForm
     * @param bindingResult
     * @return
     */
    @PostMapping(value = "/register",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse register(@Valid UserForm userForm,
                                   BindingResult bindingResult,HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            log.error("【注册参数】参数不正确, userForm={}", userForm);
           return ServerResponse.createByErrorMessage("注册参数不正确");
        }

        String otpCode = userForm.getOtpCode();
        String inSessionOtpCode = (String) request.getSession().getAttribute(userForm.getTelphone());
        if(!StringUtils.equals(otpCode,inSessionOtpCode)){
            return ServerResponse.createByErrorMessage("验证码不正确");
        }

        return userService.register(userForm);

    }


    /**
     * 用户获取otp短信接口
     * @param telphone
     * @param request
     * @return
     */
    @PostMapping(value = "/getotp",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse getotp(@RequestParam(name = "telphone") String telphone, HttpServletRequest request){
        //1.需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt = randomInt + 10000;
        String otpCode = String.valueOf(randomInt);

        //2.将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与OTPCODE
        request.getSession().setAttribute(telphone,otpCode);


        //3.通过第三方短信服务将验证码发送
        log.info("optCode = {}&&telphone = {}",otpCode,telphone);

        return ServerResponse.createBySuccess();

    }

}
