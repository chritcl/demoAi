package com.oa.platform.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果。
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页 */
    private long pageNum;
    /** 每页大小 */
    private long pageSize;
    /** 总条数 */
    private long total;
    /** 总页数 */
    private long pages;
    /** 数据列表 */
    private List<T> list;

    public PageResult() {
        this.list = Collections.emptyList();
    }

    public PageResult(long pageNum, long pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
        this.list = list == null ? Collections.emptyList() : list;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    public static <T, E> PageResult<T> of(IPage<E> page, List<T> list) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }
}
