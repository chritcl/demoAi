package com.oa.platform.security.vo;

import lombok.Data;

/**
 * 验证码响应。
 */
@Data
public class CaptchaVO {
    /** 验证码唯一标识 */
    private String uuid;
    /** base64 图片 */
    private String img;
    /** 是否启用验证码 */
    private boolean captchaEnabled;
}
