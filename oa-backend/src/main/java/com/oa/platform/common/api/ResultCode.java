package com.oa.platform.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统统一响应码。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    TOKEN_INVALID(401, "无效的令牌"),
    TOKEN_BLACKLIST(401, "令牌已注销"),

    BAD_REQUEST(400, "请求参数错误"),
    PARAM_VALIDATE_ERROR(400, "参数校验失败"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),

    BUSINESS_ERROR(1000, "业务异常"),
    LOGIN_ERROR(1001, "用户名或密码错误"),
    CAPTCHA_ERROR(1002, "验证码错误或已失效"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_LOCKED(1004, "账号已被锁定"),
    DATA_EXISTS(1005, "数据已存在"),
    DATA_NOT_EXISTS(1006, "数据不存在"),
    FLOW_ERROR(1007, "流程处理异常");

    private final int code;
    private final String msg;

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
