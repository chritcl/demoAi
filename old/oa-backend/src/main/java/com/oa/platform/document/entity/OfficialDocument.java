package com.oa.platform.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 公文（发文/收文）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_official")
public class OfficialDocument extends BaseEntity {

    /** send 发文 / receive 收文 */
    private String docType;

    /** 文号 */
    private String docNo;

    /** 文种（字典 oa_doc_category） */
    private String docCategory;

    /** 年度流水号(用于文号生成) */
    private Integer seqNo;

    /** 标题 */
    private String title;

    /** 缓急 0普通 1紧急 2特急 */
    private Integer urgency;

    /** 密级 0公开 1秘密 2机密 */
    private Integer secrecy;

    /** 正文(富文本) */
    private String content;

    /** 附件(fileId 列表 JSON) */
    private String attachments;

    /** 拟稿人ID */
    private Long drafterUserId;

    /** 拟稿人姓名 */
    private String drafterName;

    /** 拟稿部门ID */
    private Long deptId;

    /** 来文单位(收文) */
    private String fromUnit;

    /** 发送范围 */
    private String recipientScope;

    /** 状态 0草稿 1审批中 2已发布 3已驳回（收文:1已登记） */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;
}
