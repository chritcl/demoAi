package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 办公用品。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_supply")
public class OfficeSupply extends BaseEntity {

    /** 名称 */
    private String name;

    /** 分类（字典 oa_supply_category） */
    private String category;

    /** 规格 */
    private String spec;

    /** 单位 */
    private String unit;

    /** 库存数量 */
    private Integer stock;

    /** 库存预警 */
    private Integer warningStock;

    /** 单价 */
    private BigDecimal price;
}
