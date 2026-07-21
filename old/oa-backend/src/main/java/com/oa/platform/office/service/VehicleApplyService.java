package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.office.entity.VehicleApply;
import com.oa.platform.office.mapper.VehicleApplyMapper;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.stereotype.Service;

/**
 * 用车申请服务。
 */
@Service
public class VehicleApplyService extends AbstractApplyService<VehicleApply> {

    private final VehicleApplyMapper mapper;

    public VehicleApplyService(VehicleApplyMapper mapper, FlowService flowService) {
        super(flowService);
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<VehicleApply> mapper() {
        return mapper;
    }

    @Override
    protected String businessType() {
        return "vehicle";
    }

    @Override
    protected String flowKey() {
        return "vehicle";
    }

    @Override
    protected String title(VehicleApply entity) {
        return "用车申请：" + entity.getApplicantName() + " " + entity.getDestination();
    }

    @Override
    protected void beforeCreate(VehicleApply entity) {
        entity.setApplicantId(SecurityUtils.getCurrentUserId());
        entity.setApplicantName(SecurityUtils.getLoginUser().getNickname());
        entity.setDeptId(SecurityUtils.getCurrentDeptId());
    }

    @Override
    protected Integer statusOf(VehicleApply entity) {
        return entity.getStatus();
    }

    @Override
    protected void assignStatus(VehicleApply entity, Integer status) {
        entity.setStatus(status);
    }
}
