package com.oa.platform.system.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.R;
import com.oa.platform.system.entity.SysDept;
import com.oa.platform.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理。
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    private final SysDeptService deptService;

    public SysDeptController(SysDeptService deptService) {
        this.deptService = deptService;
    }

    @Operation(summary = "部门树")
    @PreAuthorize("@ss.hasPerm('system:dept:list')")
    @GetMapping("/tree")
    public R<List<SysDept>> tree(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(deptService.tree(deptName, status));
    }

    @Operation(summary = "部门下拉(全部)")
    @GetMapping("/option")
    public R<List<SysDept>> option() {
        return R.ok(deptService.tree(null, 0));
    }

    @Operation(summary = "部门详情")
    @PreAuthorize("@ss.hasPerm('system:dept:query')")
    @GetMapping("/{id}")
    public R<SysDept> detail(@PathVariable Long id) {
        return R.ok(deptService.detail(id));
    }

    @Operation(summary = "新增部门")
    @OperLog(title = "部门管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('system:dept:add')")
    @PostMapping
    public R<Void> create(@RequestBody SysDept dept) {
        deptService.create(dept);
        return R.ok();
    }

    @Operation(summary = "修改部门")
    @OperLog(title = "部门管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('system:dept:edit')")
    @PutMapping
    public R<Void> update(@RequestBody SysDept dept) {
        deptService.update(dept);
        return R.ok();
    }

    @Operation(summary = "删除部门")
    @OperLog(title = "部门管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('system:dept:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return R.ok();
    }
}
