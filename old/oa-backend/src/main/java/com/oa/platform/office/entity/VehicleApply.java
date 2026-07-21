package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用车申请。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_vehicle")
public class VehicleApply extends BaseEntity {

    private Long applicantId;
    private String applicantName;
    private Long deptId;

    /** 用车时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime useTime;

    /** 预计返回时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /** 出发地 */
    private String originPlace;

    /** 目的地 */
    private String destination;

    /** 随行人数 */
    private Integer passengers;

    /** 要求车型（字典 oa_vehicle_type） */
    private String carType;

    /** 事由 */
    private String reason;

    /** 状态 0草稿 1审批中 2已通过 3已驳回 */
    private Integer status;
}
