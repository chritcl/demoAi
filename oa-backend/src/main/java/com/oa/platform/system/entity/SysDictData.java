package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    /** 字典类型 */
    private String dictType;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 样式（primary/success/info/warning/danger） */
    private String listClass;

    /** 显示顺序 */
    private Integer sort;

    /** 状态 0正常 1停用 */
    private Integer status;

    /** 是否默认 0是 1否 */
    private Integer isDefault;
}
