package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模块标题 */
    private String title;

    /** 业务类型（0其它 1新增 2修改 3删除 4导出 ...） */
    private Integer businessType;

    /** 请求方法 */
    private String method;

    /** 请求方式 */
    private String requestMethod;

    /** 操作人员 */
    private String operName;

    /** 操作人员ID */
    private Long operId;

    /** 请求URL */
    private String operUrl;

    /** 请求参数 */
    private String operParam;

    /** 返回结果 */
    private String jsonResult;

    /** 操作状态 0正常 1异常 */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 操作IP */
    private String operIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operTime;

    /** 消耗(毫秒) */
    private Long costTime;
}
