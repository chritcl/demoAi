package com.oa.platform.common.exception;

import com.oa.platform.common.api.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.code();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.msg());
        this.code = resultCode.code();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.code();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
