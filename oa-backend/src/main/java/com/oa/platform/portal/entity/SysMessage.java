package com.oa.platform.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站内消息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    /** 接收人ID */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型 todo 待办 / notice 公告 / system 系统 */
    private String type;

    /** 业务类型 */
    private String businessType;

    /** 业务ID */
    private Long businessId;

    /** 是否已读 0未读 1已读 */
    private Integer isRead;

    /** 发送人ID */
    private Long sendUserId;

    /** 发送人姓名 */
    private String sendUserName;
}

