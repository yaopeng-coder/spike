package cn.hust.spike.form;

import lombok.Data;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 15:36
 **/
@Data
public class UserForm {

    private String telphone;

    private String otpCode;

    private String name;

    private Integer gender;

    private Integer age;

    private String password;

}
