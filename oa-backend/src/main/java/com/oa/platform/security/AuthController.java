package com.oa.platform.security;

import com.oa.platform.common.api.R;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.security.dto.LoginDTO;
import com.oa.platform.security.service.AuthService;
import com.oa.platform.security.service.TokenService;
import com.oa.platform.security.vo.CaptchaVO;
import com.oa.platform.security.vo.LoginVO;
import com.oa.platform.security.vo.UserInfoVO;
import com.oa.platform.system.service.SysMenuService;
import com.oa.platform.system.vo.RouterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证相关接口：登录/登出/验证码/当前用户/动态路由。
 */
@Tag(name = "认证", description = "登录、登出、当前用户、验证码")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final SysMenuService menuService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, TokenService tokenService,
                          SysMenuService menuService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.menuService = menuService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<CaptchaVO> captcha() {
        return R.ok(authService.captcha());
    }

    @Operation(summary = "是否开启验证码")
    @GetMapping("/captcha-enabled")
    public R<Boolean> captchaEnabled() {
        return R.ok(authService.captchaEnabled());
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String header = request.getHeader(jwtUtil.getHeader());
        if (header != null && header.startsWith(jwtUtil.getTokenPrefix())) {
            tokenService.logout(header.substring(jwtUtil.getTokenPrefix().length()).trim());
        }
        return R.ok();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public R<UserInfoVO> info() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(loginUser.getUserId());
        vo.setUsername(loginUser.getUsername());
        vo.setNickname(loginUser.getNickname());
        vo.setAvatar(loginUser.getAvatar());
        vo.setDeptId(loginUser.getDeptId());
        vo.setDeptName(loginUser.getDeptName());
        vo.setRoles(loginUser.getRoles());
        vo.setPermissions(loginUser.getPermissions());
        return R.ok(vo);
    }

    @Operation(summary = "获取当前用户的动态路由")
    @GetMapping("/routers")
    public R<List<RouterVO>> routers() {
        return R.ok(menuService.buildRouters(SecurityUtils.getCurrentUserId()));
    }
}
