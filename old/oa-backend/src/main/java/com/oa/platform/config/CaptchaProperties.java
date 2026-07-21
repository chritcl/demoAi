package com.oa.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oa.captcha")
public class CaptchaProperties {
    /** 是否开启验证码 */
    private boolean enabled = true;
}
