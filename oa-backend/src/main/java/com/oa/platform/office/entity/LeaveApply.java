package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 请休假申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_leave")
public class LeaveApply extends BaseEntity {

    /** 申请人ID */
    private Long applicantId;

    /** 申请人姓名 */
    private String applicantName;

    /** 部门ID */
    private Long deptId;

    /** 假别（字典 oa_leave_type） */
    private String leaveType;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 天数 */
    private java.math.BigDecimal days;

    /** 请假事由 */
    private String reason;

    /** 代班/交接人ID */
    private Long proxyUserId;

    /** 状态 0草稿 1审批中 2已通过 3已驳回 */
    private Integer status;
}
