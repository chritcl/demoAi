package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.office.entity.TripApply;
import com.oa.platform.office.mapper.TripApplyMapper;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.stereotype.Service;

/**
 * 出差申请服务。
 */
@Service
public class TripApplyService extends AbstractApplyService<TripApply> {

    private final TripApplyMapper mapper;

    public TripApplyService(TripApplyMapper mapper, FlowService flowService) {
        super(flowService);
        this.mapper = mapper;
    }

    @Override
    protected BaseMapper<TripApply> mapper() {
        return mapper;
    }

    @Override
    protected String businessType() {
        return "trip";
    }

    @Override
    protected String flowKey() {
        return "trip";
    }

    @Override
    protected String title(TripApply entity) {
        return "出差申请：" + entity.getApplicantName() + " " + (entity.getDestination() == null ? "" : entity.getDestination());
    }

    @Override
    protected void beforeCreate(TripApply entity) {
        entity.setApplicantId(SecurityUtils.getCurrentUserId());
        entity.setApplicantName(SecurityUtils.getLoginUser().getNickname());
        entity.setDeptId(SecurityUtils.getCurrentDeptId());
    }

    @Override
    protected Integer statusOf(TripApply entity) {
        return entity.getStatus();
    }

    @Override
    protected void assignStatus(TripApply entity, Integer status) {
        entity.setStatus(status);
    }
}
