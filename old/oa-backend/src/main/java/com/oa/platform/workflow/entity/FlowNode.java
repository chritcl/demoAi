package com.oa.platform.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程节点。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("flow_node")
public class FlowNode extends BaseEntity {

    /** 所属流程定义ID */
    private Long flowId;

    /** 节点名称 */
    private String nodeName;

    /** 审批人类型 user 指定用户 / role 角色(取该角色下一名用户) / initiator 发起人自审批 */
    private String approverType;

    /** 审批人值（userId / roleKey） */
    private String approverValue;

    /** 审批人显示名 */
    private String approverName;

    /** 顺序 */
    private Integer sort;
}
