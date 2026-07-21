package com.oa.platform.office.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.LeaveApply;
import com.oa.platform.office.service.LeaveApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 请休假管理。
 */
@Tag(name = "请休假管理")
@RestController
@RequestMapping("/office/leave")
public class LeaveApplyController {

    private final LeaveApplyService service;

    public LeaveApplyController(LeaveApplyService service) {
        this.service = service;
    }

    @Operation(summary = "分页查询")
    @PreAuthorize("@ss.hasPerm('office:leave:list')")
    @GetMapping("/page")
    public R<PageResult<LeaveApply>> page(PageQuery pq,
                                          @RequestParam(required = false) Long applicantId,
                                          @RequestParam(required = false) Integer status) {
        return R.ok(service.page(pq, applicantId, status));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<LeaveApply> detail(@PathVariable Long id) {
        return R.ok(service.detail(id));
    }

    @Operation(summary = "新增(草稿)")
    @OperLog(title = "请假申请", businessType = 1)
    @PreAuthorize("@ss.hasPerm('office:leave:add')")
    @PostMapping
    public R<Long> create(@RequestBody LeaveApply apply) {
        return R.ok(service.create(apply));
    }

    @Operation(summary = "修改")
    @OperLog(title = "请假申请", businessType = 2)
    @PreAuthorize("@ss.hasPerm('office:leave:edit')")
    @PutMapping
    public R<Void> update(@RequestBody LeaveApply apply) {
        service.update(apply);
        return R.ok();
    }

    @Operation(summary = "提交审批")
    @OperLog(title = "请假申请", businessType = 2)
    @PreAuthorize("@ss.hasPerm('office:leave:submit')")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        service.submit(id);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(title = "请假申请", businessType = 3)
    @PreAuthorize("@ss.hasPerm('office:leave:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }
}
