package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 出差申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_trip")
public class TripApply extends BaseEntity {

    private Long applicantId;
    private String applicantName;
    private Long deptId;

    /** 目的地 */
    private String destination;

    /** 出差开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 出差结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 天数 */
    private BigDecimal days;

    /** 交通方式（字典 oa_travel_mode） */
    private String travelMode;

    /** 预算(元) */
    private BigDecimal budget;

    /** 事由 */
    private String reason;

    /** 状态 0草稿 1审批中 2已通过 3已驳回 */
    private Integer status;
}
