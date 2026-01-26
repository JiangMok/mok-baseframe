package com.mok.baseframe.ratelimiter.exception;

/**
 * 重复提交异常
 */
public class DuplicateSubmitException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String code;
    
    public DuplicateSubmitException(String message) {
        super(message);
        this.code = "DUPLICATE_SUBMIT";
    }
    
    public DuplicateSubmitException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}