package com.oa.platform.office.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 办公资产。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("office_asset")
public class Asset extends BaseEntity {

    /** 资产编号 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 分类（字典 oa_asset_category） */
    private String category;

    /** 规格型号 */
    private String spec;

    /** 计量单位 */
    private String unit;

    /** 数量 */
    private Integer quantity;

    /** 价值(元) */
    private BigDecimal amount;

    /** 存放/使用地点 */
    private String location;

    /** 使用部门ID */
    private Long useDeptId;

    /** 使用人ID */
    private Long useUserId;

    /** 状态 0闲置 1在用 2维修 3报废 */
    private Integer status;
}
