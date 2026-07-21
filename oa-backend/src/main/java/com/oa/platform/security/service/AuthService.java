package com.oa.platform.security.service;

import cn.hutool.core.util.IdUtil;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.util.IpUtils;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.util.RedisUtil;
import com.oa.platform.config.CaptchaProperties;
import com.oa.platform.security.LoginUser;
import com.oa.platform.security.dto.LoginDTO;
import com.oa.platform.security.vo.CaptchaVO;
import com.oa.platform.security.vo.LoginVO;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.mapper.SysUserMapper;
import com.wf.captcha.SpecCaptcha;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证服务：验证码、登录。
 */
@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final RedisUtil redis;
    private final CaptchaProperties captchaProperties;
    private final SysUserMapper userMapper;

    public AuthService(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                       TokenService tokenService, RedisUtil redis,
                       CaptchaProperties captchaProperties, SysUserMapper userMapper) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.redis = redis;
        this.captchaProperties = captchaProperties;
        this.userMapper = userMapper;
    }

    /**
     * 生成图形验证码。
     */
    public CaptchaVO captcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String text = captcha.text().toLowerCase();
        String uuid = IdUtil.fastSimpleUUID();
        redis.set(Constants.CACHE_CAPTCHA + uuid, text, Duration.ofMinutes(5));
        CaptchaVO vo = new CaptchaVO();
        vo.setUuid(uuid);
        vo.setImg(captcha.toBase64());
        vo.setCaptchaEnabled(captchaProperties.isEnabled());
        return vo;
    }

    public boolean captchaEnabled() {
        return captchaProperties.isEnabled();
    }

    /**
     * 登录。
     */
    public LoginVO login(LoginDTO dto) {
        // 校验验证码
        if (captchaProperties.isEnabled()) {
            String cached = redis.get(Constants.CACHE_CAPTCHA + dto.getUuid());
            redis.delete(Constants.CACHE_CAPTCHA + dto.getUuid());
            if (cached == null || dto.getCode() == null || !cached.equalsIgnoreCase(dto.getCode())) {
                throw new BusinessException(ResultCode.CAPTCHA_ERROR);
            }
        }
        // 加载用户（用户不存在/停用会抛业务异常）
        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(dto.getUsername());
        if (!passwordEncoder.matches(dto.getPassword(), loginUser.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        // 更新登录信息
        SysUser update = new SysUser();
        update.setId(loginUser.getUserId());
        update.setLoginDate(LocalDateTime.now());
        update.setLoginIp(getClientIp());
        userMapper.updateById(update);

        String token = tokenService.createToken(loginUser);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return vo;
    }

    /**
     * 登出。
     */
    public void logout(String token) {
        tokenService.logout(token);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return IpUtils.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
