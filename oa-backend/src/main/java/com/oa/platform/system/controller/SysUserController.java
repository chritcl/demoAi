package com.oa.platform.system.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.dto.SysUserDTO;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理。
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService userService;

    public SysUserController(SysUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户分页查询")
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    @GetMapping("/page")
    public R<PageResult<SysUser>> page(PageQuery pq,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) Long deptId) {
        return R.ok(userService.page(pq, username, phone, status, deptId));
    }

    @Operation(summary = "用户详情(含角色)")
    @PreAuthorize("@ss.hasPerm('system:user:query')")
    @GetMapping("/{id}")
    public R<SysUser> detail(@PathVariable Long id) {
        return R.ok(userService.detail(id));
    }

    @Operation(summary = "用户角色ID列表")
    @GetMapping("/{id}/roles")
    public R<List<Long>> roles(@PathVariable Long id) {
        return R.ok(userService.getRoleIds(id));
    }

    @Operation(summary = "新增用户")
    @OperLog(title = "用户管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:user:add')")
    @PostMapping
    public R<Void> create(@RequestBody @Valid SysUserDTO dto) {
        userService.create(dto);
        return R.ok();
    }

    @Operation(summary = "修改用户")
    @OperLog(title = "用户管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysUserDTO dto) {
        userService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @OperLog(title = "用户管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:user:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @Operation(summary = "重置密码")
    @OperLog(title = "用户管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:user:resetPwd')")
    @PutMapping("/{id}/reset-pwd")
    public R<Void> resetPwd(@PathVariable Long id, @RequestParam String password) {
        userService.resetPwd(id, password);
        return R.ok();
    }

    @Operation(summary = "修改状态")
    @OperLog(title = "用户管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @PutMapping("/{id}/status")
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.changeStatus(id, status);
        return R.ok();
    }

    @Operation(summary = "个人信息")
    @GetMapping("/profile")
    public R<SysUser> profile() {
        return R.ok(userService.profile());
    }

    @Operation(summary = "修改个人信息")
    @OperLog(title = "个人信息", businessType = 2)
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SysUser dto) {
        userService.updateProfile(dto);
        return R.ok();
    }

    @Operation(summary = "修改密码")
    @OperLog(title = "修改密码", businessType = 2)
    @PutMapping("/profile/password")
    public R<Void> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.updatePassword(oldPassword, newPassword);
        return R.ok();
    }
}
