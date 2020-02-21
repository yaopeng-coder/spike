package cn.hust.spike.service.impl;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.converter.UserForm2User;
import cn.hust.spike.dao.UserMapper;
import cn.hust.spike.entity.User;
import cn.hust.spike.form.UserForm;
import cn.hust.spike.service.IUserService;
import cn.hust.spike.util.MD5Util;
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
    public ServerResponse register(UserForm userForm) {

        //插入用户表
        String password = userForm.getPassword();
        String newPassword = MD5Util.MD5EncodeUtf8(password);
        User user = UserForm2User.conver(userForm);
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
}
