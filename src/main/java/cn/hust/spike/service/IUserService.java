package cn.hust.spike.service;

import cn.hust.spike.common.ServerResponse;
import cn.hust.spike.dto.UserDTO;
import cn.hust.spike.entity.User;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 14:19
 **/
public interface IUserService {

    ServerResponse register(UserDTO userForm);
    ServerResponse login(String telphone, String password);
    User selectUserCacheById(Integer userId);
}
