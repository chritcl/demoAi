package com.oa.platform.common.page;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基类。
 */
@Data
public class PageQuery implements Serializable {

    /** 当前页码（默认 1） */
    private Integer pageNum = 1;

    /** 每页条数（默认 10） */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderField;

    /** 排序方向 asc/desc */
    private String orderAsc = "desc";

    /**
     * 构建 MyBatis-Plus 分页对象。
     */
    public <T> Page<T> toPage() {
        int num = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);
        Page<T> page = new Page<>(num, size);
        if (orderField != null && !orderField.isBlank()) {
            String col = camelToUnderline(orderField);
            page.addOrder("desc".equalsIgnoreCase(orderAsc) ? OrderItem.desc(col) : OrderItem.asc(col));
        } else {
            page.addOrder(OrderItem.desc("id"));
        }
        return page;
    }

    /** 驼峰转下划线，简单实现，仅用于排序字段。 */
    private String camelToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
