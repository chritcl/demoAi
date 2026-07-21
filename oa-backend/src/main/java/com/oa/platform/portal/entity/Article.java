package com.oa.platform.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 信息发布文章。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("portal_article")
public class Article extends BaseEntity {

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 正文 */
    private String content;

    /** 栏目（字典 oa_article_category） */
    private String category;

    /** 状态 0草稿 1待审核 2已发布 3已驳回 */
    private Integer status;

    /** 是否置顶 0否 1是 */
    private Integer top;

    /** 作者 */
    private String author;

    /** 封面 */
    private String cover;

    /** 浏览次数 */
    private Integer viewCount;

    /** 发布人ID */
    private Long publishUserId;

    /** 审核意见 */
    private String auditComment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
