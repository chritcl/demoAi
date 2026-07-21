package com.oa.platform.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件记录。
 */
@Data
@TableName("sys_file")
public class SysFile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名 */
    private String fileName;

    /** 相对路径 */
    private String filePath;

    /** 访问URL */
    private String url;

    /** 文件大小(字节) */
    private Long size;

    /** 文件类型(MIME) */
    private String contentType;

    /** 上传人ID */
    private Long createBy;

    private LocalDate uploadDay;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
