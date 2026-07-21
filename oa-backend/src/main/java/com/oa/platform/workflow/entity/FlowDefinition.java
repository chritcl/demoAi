package com.oa.platform.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 流程定义。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_definition")
public class FlowDefinition extends BaseEntity {

    /** 流程标识（业务唯一，如 leave/vehicle/document_send） */
    private String flowKey;

    /** 流程名称 */
    private String flowName;

    /** 业务类型（与 flowKey 一致，冗余便于查询） */
    private String businessType;

    /** 版本 */
    private Integer version;

    /** 状态 0启用 1停用 */
    private Integer status;

    /** 流程节点（非持久化） */
    @TableField(exist = false)
    private List<FlowNode> nodes;
}
