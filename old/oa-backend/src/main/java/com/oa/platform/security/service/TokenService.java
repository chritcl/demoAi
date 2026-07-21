package com.oa.platform.security.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.util.RedisUtil;
import com.oa.platform.security.JwtProperties;
import com.oa.platform.security.JwtUtil;
import com.oa.platform.security.LoginUser;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录令牌服务：生成 token、缓存登录用户、登出黑名单。
 */
@Service
public class TokenService {

    private final JwtUtil jwtUtil;
    private final JwtProperties properties;
    private final RedisUtil redis;
    private final ObjectMapper objectMapper = new ObjectMapper()
            // 反序列化时忽略未知属性（如 isAdmin() 派生的 "admin" 字段），避免抛 UnrecognizedPropertyException
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public TokenService(JwtUtil jwtUtil, JwtProperties properties, RedisUtil redis) {
        this.jwtUtil = jwtUtil;
        this.properties = properties;
        this.redis = redis;
    }

    /**
     * 登录成功后创建 token 并缓存用户信息。
     */
    public String createToken(LoginUser loginUser) {
        String token = jwtUtil.createToken(loginUser.getUserId(), loginUser.getUsername());
        try {
            String json = objectMapper.writeValueAsString(loginUser);
            redis.set(loginKey(token), json, properties.getExpire());
        } catch (Exception e) {
            throw new IllegalStateException("缓存登录用户失败", e);
        }
        return token;
    }

    /**
     * 根据 token 读取登录用户（命中即续期）。
     */
    public LoginUser getLoginUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (isBlacklisted(token)) {
            return null;
        }
        String json = redis.get(loginKey(token));
        if (json == null) {
            return null;
        }
        try {
            LoginUser loginUser = objectMapper.readValue(json, LoginUser.class);
            // 续期
            redis.expire(loginKey(token), properties.getExpire());
            return loginUser;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 刷新缓存（用户信息变更后调用）。
     */
    public void refresh(String token, LoginUser loginUser) {
        try {
            String json = objectMapper.writeValueAsString(loginUser);
            redis.set(loginKey(token), json, properties.getExpire());
        } catch (Exception ignored) {
        }
    }

    /**
     * 登出：删除缓存并加入黑名单。
     */
    public void logout(String token) {
        if (token == null) {
            return;
        }
        redis.delete(loginKey(token));
        redis.set(blacklistKey(token), "1", properties.getExpire());
    }

    public boolean isBlacklisted(String token) {
        return redis.exists(blacklistKey(token));
    }

    private String loginKey(String token) {
        return Constants.CACHE_LOGIN_USER + token;
    }

    private String blacklistKey(String token) {
        return Constants.CACHE_TOKEN_BLACKLIST + token;
    }
}
