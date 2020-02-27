package cn.hust.spike.controller;

import cn.hust.spike.Common.Const;
import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dto.UserDTO;
import cn.hust.spike.entity.User;
import cn.hust.spike.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;



    /**
     * 注册用户
     * @param userForm
     * @param bindingResult
     * @return
     */
    @PostMapping(value = "/register",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse register(@Valid UserDTO userForm,
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

    /**
     * 用户登陆
     * @param telphone
     * @param password
     * @return
     */
    @PostMapping(value = "/login",consumes = {Const.CONTENT_TYPE_FORMED} )
    public ServerResponse login(@RequestParam(name = "telphone") String telphone, @RequestParam(name = "password") String password,HttpServletRequest request){
        //1.校验参数是否为空
        if(StringUtils.isBlank(telphone) || StringUtils.isBlank(password)){
            return ServerResponse.createByErrorMessage("参数为空");
        }



        //2.将用户信息存储进session
        ServerResponse<User> serverResponse =  userService.login(telphone,password);

        if(serverResponse.isSuccess()){
           // request.getSession().setAttribute(Const.CURRENT_USER,serverResponse.getData());
            //生成登录凭证token，UUID
            String uuidToken = UUID.randomUUID().toString();
            uuidToken = uuidToken.replace("-","");
            //建议token和用户登陆态之间的联系
            redisTemplate.opsForValue().set(uuidToken,serverResponse.getData());
            redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);
            return ServerResponse.createBySuccess(uuidToken);
        }




        return ServerResponse.createByError();


    }

}
