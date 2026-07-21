package com.oa.platform.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * JWT 配置（oa.jwt）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "oa.jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret = "oa-platform-secret-key";

    /** token 有效期（默认单位：分钟） */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration expire = Duration.ofMinutes(720);

    /** 请求头名称 */
    private String header = "Authorization";

    /** token 前缀 */
    private String tokenPrefix = "Bearer ";
}
