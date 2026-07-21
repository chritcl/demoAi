package com.oa.platform.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程任务（待办）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_task")
public class FlowTask extends BaseEntity {

    /** 实例ID */
    private Long instanceId;

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

    /** 节点名称 */
    private String nodeName;

    /** 节点顺序 */
    private Integer nodeSort;

    /** 办理人ID */
    private Long assignee;

    /** 办理人姓名 */
    private String assigneeName;

    /** 发起人ID */
    private Long startUserId;

    /** 状态 pending/done/rejected/transferred */
    private String status;

    /** 审批意见 */
    private String comment;

    /** 办理人ID */
    private Long actionUserId;

    /** 办理人姓名 */
    private String actionUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actionTime;
}
