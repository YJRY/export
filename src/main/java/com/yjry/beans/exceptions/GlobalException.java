package com.yjry.beans.exceptions;
/**
 * 全局异常
 * @author xuqi
 * @date 2019-10-15 16:54:41
 */
public class GlobalException extends RuntimeException {
    public GlobalException() {
        super();
    }

    public GlobalException(String message) {
        super(message);
    }
}
