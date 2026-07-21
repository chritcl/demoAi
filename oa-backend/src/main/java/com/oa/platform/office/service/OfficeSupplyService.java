package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.OfficeSupply;
import com.oa.platform.office.mapper.OfficeSupplyMapper;
import org.springframework.stereotype.Service;

/**
 * 办公用品服务。
 */
@Service
public class OfficeSupplyService {

    private final OfficeSupplyMapper mapper;

    public OfficeSupplyService(OfficeSupplyMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<OfficeSupply> page(PageQuery pq, String name, String category) {
        Page<OfficeSupply> page = pq.toPage();
        LambdaQueryWrapper<OfficeSupply> w = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            w.like(OfficeSupply::getName, name);
        }
        if (category != null && !category.isBlank()) {
            w.eq(OfficeSupply::getCategory, category);
        }
        w.orderByDesc(OfficeSupply::getId);
        return PageResult.of(mapper.selectPage(page, w));
    }

    public OfficeSupply detail(Long id) {
        OfficeSupply s = mapper.selectById(id);
        if (s == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return s;
    }

    public void create(OfficeSupply supply) {
        mapper.insert(supply);
    }

    public void update(OfficeSupply supply) {
        mapper.updateById(supply);
    }

    /** 出入库：amount 正入库 负出库 */
    public void adjustStock(Long id, int amount) {
        OfficeSupply s = detail(id);
        int newStock = (s.getStock() == null ? 0 : s.getStock()) + amount;
        if (newStock < 0) {
            throw new BusinessException("库存不足");
        }
        OfficeSupply upd = new OfficeSupply();
        upd.setId(id);
        upd.setStock(newStock);
        mapper.updateById(upd);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
