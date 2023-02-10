package com.xuecheng.content.base.execption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

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

    /**
     * 解决 MethodArgumentNotValidException 异常，由参数校验引起的异常
     * @param e MethodArgumentNotValidException
     * @return error
     */
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseError DoMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach( item -> {
            String defaultMessage = item.getDefaultMessage();
            errorMessage.append(defaultMessage).append(",");
        });
        errorMessage.deleteCharAt(errorMessage.length() - 1);
        return new ResponseError(errorMessage.toString());
    }

}
