package com.oa.platform.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 通知公告。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("portal_notice")
public class Notice extends BaseEntity {

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文(富文本) */
    private String content;

    /** 公告分类（字典 oa_notice_type） */
    private String category;

    /** 状态 0草稿 1已发布 2已撤回 */
    private Integer status;

    /** 是否置顶 0否 1是 */
    private Integer top;

    /** 封面图(fileId/url) */
    private String cover;

    /** 阅读次数 */
    private Integer readCount;

    /** 发布人ID */
    private Long publishUserId;

    /** 发布人姓名 */
    private String publishUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
