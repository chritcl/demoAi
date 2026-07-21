package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.entity.BaseEntity;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.workflow.event.FlowCompletedEvent;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * 综合办公「审批申请」类业务通用基类。
 * <p>约定子实体需包含 applicant_id / applicant_name / dept_id / status 列。</p>
 */
public abstract class AbstractApplyService<T extends BaseEntity> {

    protected final FlowService flowService;

    protected AbstractApplyService(FlowService flowService) {
        this.flowService = flowService;
    }

    protected abstract BaseMapper<T> mapper();

    protected abstract String businessType();

    protected abstract String flowKey();

    /** 提交流程时的标题 */
    protected abstract String title(T entity);

    /** insert 前设置申请人等公共字段 */
    protected abstract void beforeCreate(T entity);

    /** 读取业务状态 */
    protected abstract Integer statusOf(T entity);

    /** 设置业务状态(写入实体) */
    protected abstract void assignStatus(T entity, Integer status);

    public PageResult<T> page(PageQuery pq, Long applicantId, Integer status, boolean mineOnly) {
        Page<T> page = pq.toPage();
        QueryWrapper<T> w = new QueryWrapper<>();
        if (applicantId != null) {
            w.eq("applicant_id", applicantId);
        }
        if (status != null) {
            w.eq("status", status);
        }
        if (mineOnly && applicantId == null && !SecurityUtils.isAdmin()) {
            w.eq("applicant_id", SecurityUtils.getCurrentUserId());
        }
        w.orderByDesc("id");
        return PageResult.of(mapper().selectPage(page, w));
    }

    public T detail(Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return entity;
    }

    public Long create(T entity) {
        beforeCreate(entity);
        if (statusOf(entity) == null) {
            assignStatus(entity, 0);
        }
        mapper().insert(entity);
        return entity.getId();
    }

    public void update(T entity) {
        mapper().updateById(entity);
    }

    public void delete(Long id) {
        mapper().deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        Integer status = statusOf(entity);
        if (status != null && status != 0) {
            throw new BusinessException("当前状态不可提交");
        }
        assignStatus(entity, 1);
        mapper().updateById(entity);
        flowService.start(flowKey(), id, title(entity));
    }

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onFlowCompleted(FlowCompletedEvent event) {
        if (!businessType().equals(event.getBusinessType())) {
            return;
        }
        T entity = mapper().selectById(event.getBusinessId());
        if (entity == null) {
            return;
        }
        assignStatus(entity, event.isApproved() ? 2 : 3);
        mapper().updateById(entity);
    }
}
