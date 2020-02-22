package cn.hust.spike.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-21 15:36
 **/
@Data
public class UserDTO {


    @NotBlank(message = "手机号不能为空")
    private String telphone;

    @NotBlank(message = "验证码不能为空")
    private String otpCode;

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "年龄不能为空")
    private Integer age;

    @NotBlank(message = "密码不能为空")
    private String password;

}
