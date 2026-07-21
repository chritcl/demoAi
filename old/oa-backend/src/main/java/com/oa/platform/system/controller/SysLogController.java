package com.oa.platform.system.controller;

import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysOperLog;
import com.oa.platform.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志。
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/system/log")
public class SysLogController {

    private final SysLogService logService;

    public SysLogController(SysLogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "日志分页")
    @PreAuthorize("@ss.hasPerm('system:log:list')")
    @GetMapping("/page")
    public R<PageResult<SysOperLog>> page(PageQuery pq,
                                          @RequestParam(required = false) String title,
                                          @RequestParam(required = false) String operName,
                                          @RequestParam(required = false) Integer status) {
        return R.ok(logService.page(pq, title, operName, status));
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("@ss.hasPerm('system:log:remove')")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        logService.clear();
        return R.ok();
    }

    @Operation(summary = "删除日志")
    @PreAuthorize("@ss.hasPerm('system:log:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        logService.delete(id);
        return R.ok();
    }
}
