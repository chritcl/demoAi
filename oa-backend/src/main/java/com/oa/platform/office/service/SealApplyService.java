package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.office.entity.SealApply;
import com.oa.platform.office.mapper.SealApplyMapper;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.stereotype.Service;

/**
 * 用印申请服务。
 */
@Service
public class SealApplyService extends AbstractApplyService<SealApply> {

    private final SealApplyMapper mapper;

    public SealApplyService(SealApplyMapper mapper, FlowService flowService) {
        super(flowService);
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<SealApply> mapper() {
        return mapper;
    }

    @Override
    protected String businessType() {
        return "seal";
    }

    @Override
    protected String flowKey() {
        return "seal";
    }

    @Override
    protected String title(SealApply entity) {
        return "用印申请：" + entity.getApplicantName() + " " + (entity.getSealType() == null ? "" : entity.getSealType());
    }

    @Override
    protected void beforeCreate(SealApply entity) {
        entity.setApplicantId(SecurityUtils.getCurrentUserId());
        entity.setApplicantName(SecurityUtils.getLoginUser().getNickname());
        entity.setDeptId(SecurityUtils.getCurrentDeptId());
    }

    @Override
    protected Integer statusOf(SealApply entity) {
        return entity.getStatus();
    }

    @Override
    protected void assignStatus(SealApply entity, Integer status) {
        entity.setStatus(status);
    }
}
