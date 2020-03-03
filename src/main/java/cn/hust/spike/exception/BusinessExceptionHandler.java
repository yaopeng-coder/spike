package cn.hust.spike.exception;

import cn.hust.spike.Common.ServerResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-03 13:57
 **/
@ControllerAdvice
public class BusinessExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ServerResponse handlerSellerException(BusinessException e) {
        return ServerResponse.createByErrorMessage( e.getMessage());
    }
}
