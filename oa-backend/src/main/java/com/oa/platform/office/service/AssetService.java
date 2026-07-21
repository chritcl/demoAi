package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.Asset;
import com.oa.platform.office.mapper.AssetMapper;
import org.springframework.stereotype.Service;

/**
 * 资产管理服务。
 */
@Service
public class AssetService {

    private final AssetMapper mapper;

    public AssetService(AssetMapper mapper) {
        this.mapper = mapper;
    }

    public PageResult<Asset> page(PageQuery pq, String assetName, String category, Integer status) {
        Page<Asset> page = pq.toPage();
        LambdaQueryWrapper<Asset> w = new LambdaQueryWrapper<>();
        if (assetName != null && !assetName.isBlank()) {
            w.like(Asset::getAssetName, assetName);
        }
        if (category != null && !category.isBlank()) {
            w.eq(Asset::getCategory, category);
        }
        if (status != null) {
            w.eq(Asset::getStatus, status);
        }
        w.orderByDesc(Asset::getId);
        return PageResult.of(mapper.selectPage(page, w));
    }

    public Asset detail(Long id) {
        Asset asset = mapper.selectById(id);
        if (asset == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return asset;
    }

    public void create(Asset asset) {
        mapper.insert(asset);
    }

    public void update(Asset asset) {
        mapper.updateById(asset);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
