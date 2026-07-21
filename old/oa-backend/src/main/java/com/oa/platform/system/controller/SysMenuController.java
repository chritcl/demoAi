package com.oa.platform.system.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.R;
import com.oa.platform.system.entity.SysMenu;
import com.oa.platform.system.service.SysMenuService;
import com.oa.platform.system.vo.RouterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理。
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    private final SysMenuService menuService;

    public SysMenuController(SysMenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "菜单列表(平铺)")
    @PreAuthorize("@ss.hasPerm('system:menu:list')")
    @GetMapping("/list")
    public R<List<SysMenu>> list(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) String status) {
        return R.ok(menuService.list(menuName, status));
    }

    @Operation(summary = "当前用户可见菜单(用于菜单树)")
    @GetMapping("/my")
    public R<List<SysMenu>> myMenu() {
        return R.ok(menuService.listForCurrentUser());
    }

    @Operation(summary = "当前用户动态路由")
    @GetMapping("/routers")
    public R<List<RouterVO>> routers() {
        return R.ok(menuService.buildRouters(com.oa.platform.common.util.SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "菜单详情")
    @PreAuthorize("@ss.hasPerm('system:menu:query')")
    @GetMapping("/{id}")
    public R<SysMenu> detail(@PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @OperLog(title = "菜单管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:menu:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysMenu menu) {
        menuService.create(menu);
        return R.ok();
    }

    @Operation(summary = "修改菜单")
    @OperLog(title = "菜单管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:menu:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysMenu menu) {
        menuService.update(menu);
        return R.ok();
    }

    @Operation(summary = "删除菜单")
    @OperLog(title = "菜单管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:menu:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return R.ok();
    }
}
