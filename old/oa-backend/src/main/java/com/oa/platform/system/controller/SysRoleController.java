package com.oa.platform.system.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysRole;
import com.oa.platform.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理。
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    private final SysRoleService roleService;

    public SysRoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "角色分页")
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    @GetMapping("/page")
    public R<PageResult<SysRole>> page(PageQuery pq,
                                       @RequestParam(required = false) String roleName,
                                       @RequestParam(required = false) String roleKey,
                                       @RequestParam(required = false) Integer status) {
        return R.ok(roleService.page(pq, roleName, roleKey, status));
    }

    @Operation(summary = "全部可选角色")
    @GetMapping("/option")
    public R<List<SysRole>> options() {
        return R.ok(roleService.listAll());
    }

    @Operation(summary = "角色详情(含菜单)")
    @PreAuthorize("@ss.hasPerm('system:role:query')")
    @GetMapping("/{id}")
    public R<SysRole> detail(@PathVariable Long id) {
        return R.ok(roleService.detail(id));
    }

    @Operation(summary = "新增角色")
    @OperLog(title = "角色管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:role:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysRole role) {
        roleService.create(role);
        return R.ok();
    }

    @Operation(summary = "修改角色")
    @OperLog(title = "角色管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysRole role) {
        roleService.update(role);
        return R.ok();
    }

    @Operation(summary = "删除角色")
    @OperLog(title = "角色管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:role:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }
}
