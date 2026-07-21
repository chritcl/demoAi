package com.oa.platform.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构。
 *
 * <pre>
 * R.ok() / R.ok(data) / R.ok(msg, data)
 * R.fail() / R.fail(msg) / R.fail(ResultCode) / R.fail(code, msg)
 * </pre>
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.msg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.msg(), data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.code(), msg, data);
    }

    public static <T> R<T> fail() {
        return new R<>(ResultCode.FAIL.code(), ResultCode.FAIL.msg(), null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.FAIL.code(), msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.code(), resultCode.msg(), null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode, String msg) {
        return new R<>(resultCode.code(), msg, null);
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.code();
    }
}
