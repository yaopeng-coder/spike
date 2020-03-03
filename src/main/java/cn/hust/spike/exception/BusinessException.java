package cn.hust.spike.exception;

import cn.hust.spike.Common.ResponseCode;
import lombok.Data;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-03 13:53
 **/
@Data
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getDesc());

        this.code = responseCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
