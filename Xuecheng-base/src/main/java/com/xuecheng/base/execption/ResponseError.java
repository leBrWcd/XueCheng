package com.xuecheng.base.execption;

import java.io.Serializable;

/**
 * Description 统一错误响应
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/9
 */
public class ResponseError implements Serializable {

    /**
     * 错误响应参数包装
     */
    private String errMessage;

    public ResponseError(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }


}
