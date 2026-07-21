package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.office.entity.LeaveApply;
import com.oa.platform.office.mapper.LeaveApplyMapper;
import com.oa.platform.workflow.event.FlowCompletedEvent;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 请休假申请服务（流程驱动）。
 */
@Service
public class LeaveApplyService {

    public static final String BUSINESS_TYPE = "leave";
    public static final String FLOW_KEY = "leave";

    private final LeaveApplyMapper mapper;
    private final FlowService flowService;

    public LeaveApplyService(LeaveApplyMapper mapper, FlowService flowService) {
        this.mapper = mapper;
        this.flowService = flowService;
    }

    public PageResult<LeaveApply> page(PageQuery pq, Long applicantId, Integer status) {
        Page<LeaveApply> page = pq.toPage();
        LambdaQueryWrapper<LeaveApply> w = new LambdaQueryWrapper<>();
        if (applicantId != null) {
            w.eq(LeaveApply::getApplicantId, applicantId);
        }
        if (status != null) {
            w.eq(LeaveApply::getStatus, status);
        }
        if (!SecurityUtils.isAdmin()) {
            // 非管理员默认看自己的
            if (applicantId == null) {
                w.eq(LeaveApply::getApplicantId, SecurityUtils.getCurrentUserId());
            }
        }
        w.orderByDesc(LeaveApply::getId);
        return PageResult.of(mapper.selectPage(page, w));
    }

    public LeaveApply detail(Long id) {
        LeaveApply entity = mapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return entity;
    }

    public Long create(LeaveApply apply) {
        apply.setApplicantId(SecurityUtils.getCurrentUserId());
        apply.setApplicantName(SecurityUtils.getLoginUser().getNickname());
        apply.setDeptId(SecurityUtils.getCurrentDeptId());
        if (apply.getStatus() == null) {
            apply.setStatus(0);
        }
        mapper.insert(apply);
        return apply.getId();
    }

    public void update(LeaveApply apply) {
        mapper.updateById(apply);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        LeaveApply apply = mapper.selectById(id);
        if (apply == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (apply.getStatus() != null && apply.getStatus() != 0) {
            throw new BusinessException("当前状态不可提交");
        }
        apply.setStatus(1);
        mapper.updateById(apply);
        flowService.start(FLOW_KEY, id, "请假申请：" + apply.getApplicantName() + " "
                + (apply.getLeaveType() == null ? "" : apply.getLeaveType()) + " " + apply.getDays() + "天");
    }

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onFlowCompleted(FlowCompletedEvent event) {
        if (!BUSINESS_TYPE.equals(event.getBusinessType())) {
            return;
        }
        LeaveApply apply = mapper.selectById(event.getBusinessId());
        if (apply == null) {
            return;
        }
        LeaveApply upd = new LeaveApply();
        upd.setId(apply.getId());
        upd.setStatus(event.isApproved() ? 2 : 3);
        mapper.updateById(upd);
    }
}
