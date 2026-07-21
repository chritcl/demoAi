package com.oa.platform.office.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.entity.BaseEntity;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.service.AbstractApplyService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 审批申请类业务通用接口基类。
 */
public abstract class AbstractApplyController<T extends BaseEntity, S extends AbstractApplyService<T>> {

    protected abstract S service();

    /** 权限前缀，例如 office:vehicle */
    protected abstract String perm();

    /** 日志标题 */
    protected abstract String title();

    @Operation(summary = "分页查询")
    @PreAuthorize("@ss.hasPerm(T(com.oa.platform.office.controller.AbstractApplyController).perm(this, 'list'))")
    @GetMapping("/page")
    public R<PageResult<T>> page(PageQuery pq,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Long applicantId,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false) Integer status,
                                 @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "true") boolean mineOnly) {
        return R.ok(service().page(pq, applicantId, status, mineOnly));
    }

    @Operation(summary = "详情")
    @GetMapping("/{id}")
    public R<T> detail(@PathVariable Long id) {
        return R.ok(service().detail(id));
    }

    @Operation(summary = "新增(草稿)")
    @OperLog(businessType = 1)
    @PreAuthorize("@ss.hasPerm(T(com.oa.platform.office.controller.AbstractApplyController).perm(this, 'add'))")
    @PostMapping
    public R<Long> create(@RequestBody T entity) {
        return R.ok(service().create(entity));
    }

    @Operation(summary = "修改")
    @OperLog(businessType = 2)
    @PreAuthorize("@ss.hasPerm(T(com.oa.platform.office.controller.AbstractApplyController).perm(this, 'edit'))")
    @PutMapping
    public R<Void> update(@RequestBody T entity) {
        service().update(entity);
        return R.ok();
    }

    @Operation(summary = "提交审批")
    @OperLog(businessType = 2)
    @PreAuthorize("@ss.hasPerm(T(com.oa.platform.office.controller.AbstractApplyController).perm(this, 'submit'))")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        service().submit(id);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(businessType = 3)
    @PreAuthorize("@ss.hasPerm(T(com.oa.platform.office.controller.AbstractApplyController).perm(this, 'remove'))")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service().delete(id);
        return R.ok();
    }

    /** 拼接权限标识 */
    public static String perm(AbstractApplyController<?, ?> ctrl, String action) {
        return ctrl.perm() + ":" + action;
    }
}
