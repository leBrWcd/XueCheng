package com.xuecheng.content.base.execption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Description 全局异常处理器
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/9
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = XuechengException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError SystemError(XuechengException e) {
        log.info("【系统异常】,错误信息： {}" ,e.getMessage());
        return new ResponseError(e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError GlobalError(XuechengException e) {
        log.info("【系统异常】,错误信息： {}" ,e.getMessage());
        return new ResponseError(CommonError.UNKOWN_ERROR.getErrMessage());
    }

}
