package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户（成员）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 部门ID */
    private Long deptId;

    /** 用户名（登录账号） */
    private String username;

    /** 昵称/姓名 */
    private String nickname;

    /** 密码（BCrypt 加密） */
    @JsonIgnore
    private String password;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 性别 0男 1女 2未知 */
    private Integer gender;

    /** 状态 0正常 1停用 */
    private Integer status;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    /** 姓名拼音（通讯录搜索用，自动生成） */
    private String pinyin;
}
