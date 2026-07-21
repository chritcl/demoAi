package com.oa.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 生成与解析工具。
 */
@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        // 至少 32 字节
        byte[] secretBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes.length >= 32 ? secretBytes
                : java.util.Arrays.copyOf(secretBytes, 32));
    }

    /**
     * 生成 token。
     *
     * @param userId   用户ID
     * @param username 用户名
     */
    public String createToken(Long userId, String username) {
        return createToken(userId, username, properties.getExpire());
    }

    public String createToken(Long userId, String username, Duration ttl) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("username", username);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttl.toMillis());
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /** 解析 token，校验失败返回 null */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(Claims claims) {
        Object uid = claims.get("uid");
        return uid == null ? null : Long.valueOf(uid.toString());
    }

    public String getUsername(Claims claims) {
        Object v = claims.get("username");
        return v == null ? null : v.toString();
    }

    public Duration getExpire() {
        return properties.getExpire();
    }

    public String getHeader() {
        return properties.getHeader();
    }

    public String getTokenPrefix() {
        return properties.getTokenPrefix();
    }
}
