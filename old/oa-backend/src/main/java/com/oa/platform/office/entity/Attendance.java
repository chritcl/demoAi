package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 考勤记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_attendance")
public class Attendance extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 用户姓名 */
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 考勤日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attDate;

    /** 上班打卡 */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime clockIn;

    /** 下班打卡 */
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime clockOut;

    /** 状态 normal/late/earlyLeave/absent */
    private String status;

    /** 备注 */
    private String remark;
}
