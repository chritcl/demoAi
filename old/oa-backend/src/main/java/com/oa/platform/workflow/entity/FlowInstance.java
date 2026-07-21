package com.oa.platform.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程实例。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_instance")
public class FlowInstance extends BaseEntity {

    /** 流程定义ID */
    private Long flowId;

    /** 流程标识 */
    private String flowKey;

    /** 业务类型 */
    private String businessType;

    /** 业务数据ID */
    private Long businessId;

    /** 标题 */
    private String title;

    /** 发起人ID */
    private Long startUserId;

    /** 发起人姓名 */
    private String startUserName;

    /** 当前节点ID */
    private Long currentNodeId;

    /** 当前节点名称 */
    private String currentNodeName;

    /** 状态 running/done/terminated */
    private String status;
}
