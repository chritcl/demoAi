package com.oa.platform.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 前端路由（动态菜单）VO，结构对齐 vue-router。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouterVO {

    private String name;

    private String path;

    private String redirect;

    /** 是否隐藏 */
    private Boolean hidden;

    /** 是否缓存 */
    private Boolean keepAlive;

    /** 是否外链 */
    private Boolean isFrame;

    private Long menuId;

    private String component;

    private Meta meta;

    private List<RouterVO> children;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        private String title;
        private String icon;
        /** 权限标识 */
        private String perms;
        /** 是否不需要登录可访问 */
        private Boolean noCache;
    }
}
