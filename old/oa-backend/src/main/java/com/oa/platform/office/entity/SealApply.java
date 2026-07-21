package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用印申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_seal")
public class SealApply extends BaseEntity {

    private Long applicantId;
    private String applicantName;
    private Long deptId;

    /** 印章类型（字典 oa_seal_type，如 公章/合同章/财务章） */
    private String sealType;

    /** 用印事由类型（字典 oa_seal_matter） */
    private String matterType;

    /** 用印份数 */
    private Integer fileCount;

    /** 用途说明（避免使用 MySQL 保留字 usage） */
    private String usageDesc;

    /** 状态 0草稿 1审批中 2已通过 3已驳回 */
    private Integer status;
}
