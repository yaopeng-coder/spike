package cn.hust.spike.converter;

import cn.hust.spike.entity.User;
import cn.hust.spike.dto.UserDTO;
import org.springframework.beans.BeanUtils;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 15:48
 **/
public class UserDTO2User {

    public static User conver(UserDTO userDTO){
        User user = new User();
        BeanUtils.copyProperties(userDTO,user);
        return user;
    }
}
