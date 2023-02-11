package com.xuecheng.base.execption;

/**
 * Description 项目全局异常
 *
 * @author lebrwcd
 * @version 1.0
 * @date 2023/2/9
 */
public class XuechengException extends RuntimeException {

    private String errMessage;

    public XuechengException() {
        super();
    }

    public XuechengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XuechengException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XuechengException(errMessage);
    }


}
