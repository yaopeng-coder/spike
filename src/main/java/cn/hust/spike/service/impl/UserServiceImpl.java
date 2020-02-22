package cn.hust.spike.service.impl;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.converter.UserDTO2User;
import cn.hust.spike.dao.UserMapper;
import cn.hust.spike.entity.User;
import cn.hust.spike.dto.UserDTO;
import cn.hust.spike.service.IUserService;
import cn.hust.spike.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 14:20
 **/
@Service
public class UserServiceImpl implements IUserService {


    @Autowired
    private UserMapper userMapper;

    /**
     * 注册用户
     * @param userForm
     * @return
     */
    @Override
    @Transactional
    public ServerResponse register(UserDTO userForm) {

        //插入用户表
        String password = userForm.getPassword();
        String newPassword = MD5Util.MD5EncodeUtf8(password);
        User user = UserDTO2User.conver(userForm);
        user.setPassword(newPassword);
        user.setRegisterMode("byPhone");


        //扑捉手机号唯一索引异常
        try{
            userMapper.insertSelective(user);
        }catch (DuplicateKeyException e){
            return ServerResponse.createByErrorMessage("手机号不能重复注册");
        }

        return ServerResponse.createBySuccess();
    }

    /**
     * 用户登陆服务
     * @param telphone
     * @param password
     * @return
     */
    public ServerResponse<User> login(String telphone, String password){

        //1.根据用户手机号去查询对应用户信息
        User user = userMapper.selectByTelphone(telphone);

        if(user == null){
            return ServerResponse.createByErrorMessage("用户手机号不存在或者密码不正确");

        }

        String passwordEncode = MD5Util.MD5EncodeUtf8(password);
        if(!StringUtils.equals(passwordEncode,user.getPassword())){
            return ServerResponse.createByErrorMessage("密码不正确");
        }

        return ServerResponse.createBySuccess("登陆成功",user);


    }
}
