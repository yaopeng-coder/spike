package cn.hust.spike.converter;

import cn.hust.spike.entity.User;
import cn.hust.spike.form.UserForm;
import org.springframework.beans.BeanUtils;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 15:48
 **/
public class UserForm2User {

    public static User conver(UserForm userForm){
        User user = new User();
        BeanUtils.copyProperties(userForm,user);
        return user;
    }
}
