package com.oa.platform.common.exception;

import com.oa.platform.common.api.R;
import com.oa.platform.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("[业务异常] {} - {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常（@RequestBody） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.PARAM_VALIDATE_ERROR, msg);
    }

    /** 参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.PARAM_VALIDATE_ERROR, msg);
    }

    /** 缺少请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.BAD_REQUEST, "缺少请求参数: " + e.getParameterName());
    }

    /** 请求方式不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.METHOD_NOT_ALLOWED);
    }

    /** 权限不足 */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Void> handleAccessDenied(AccessDeniedException e) {
        return R.fail(ResultCode.FORBIDDEN);
    }

    /** 认证失败 */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuth(AuthenticationException e) {
        return R.fail(ResultCode.UNAUTHORIZED);
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("[系统异常] {} ", request.getRequestURI(), e);
        return R.fail(ResultCode.FAIL.code(), "系统异常: " + e.getMessage());
    }
}
