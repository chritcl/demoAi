package com.oa.platform.workflow.event;

import org.springframework.context.ApplicationEvent;

/**
 * 流程结束/驳回事件，业务模块可监听以更新自身状态。
 */
public class FlowCompletedEvent extends ApplicationEvent {

    private final String businessType;
    private final Long businessId;
    private final Long instanceId;
    /** true 审批通过完成，false 驳回/终止 */
    private final boolean approved;

    public FlowCompletedEvent(Object source, String businessType, Long businessId, Long instanceId, boolean approved) {
        super(source);
        this.businessType = businessType;
        this.businessId = businessId;
        this.instanceId = instanceId;
        this.approved = approved;
    }

    public String getBusinessType() {
        return businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public boolean isApproved() {
        return approved;
    }
}
