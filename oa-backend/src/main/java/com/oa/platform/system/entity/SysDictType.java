package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    /** 字典名称 */
    private String dictName;

    /** 字典类型（唯一编码） */
    private String dictType;

    /** 状态 0正常 1停用 */
    private Integer status;
}
