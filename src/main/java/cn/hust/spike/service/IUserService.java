package cn.hust.spike.service;

import cn.hust.spike.Common.ServerResponse;
import cn.hust.spike.dto.UserDTO;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 14:19
 **/
public interface IUserService {

    ServerResponse register(UserDTO userForm);
    ServerResponse login(String telphone, String password);
}
