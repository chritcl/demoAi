package com.oa.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用启用/停用状态。
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    NORMAL(0, "正常"),
    DISABLED(1, "停用");

    private final int code;
    private final String desc;

    public static String descOf(int code) {
        for (StatusEnum s : values()) {
            if (s.code == code) {
                return s.desc;
            }
        }
        return "未知";
    }
}
