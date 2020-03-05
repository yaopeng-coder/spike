package cn.hust.spike.exception;

import cn.hust.spike.common.ResponseCode;
import lombok.Data;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-03 13:53
 **/
@Data
public class BusinessException  extends Exception{

    private Integer code;

    private String message;

    public BusinessException(ResponseCode responseCode) {

        message = responseCode.getDesc();
        this.code = responseCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        message = message;
        this.code = code;
    }
}
