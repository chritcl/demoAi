package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysDictData;
import com.oa.platform.system.entity.SysDictType;
import com.oa.platform.system.mapper.SysDictDataMapper;
import com.oa.platform.system.mapper.SysDictTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务。
 */
@Service
public class SysDictService {

    private final SysDictTypeMapper typeMapper;
    private final SysDictDataMapper dataMapper;

    public SysDictService(SysDictTypeMapper typeMapper, SysDictDataMapper dataMapper) {
        this.typeMapper = typeMapper;
        this.dataMapper = dataMapper;
    }

    /* ---------- 字典类型 ---------- */

    public PageResult<SysDictType> pageType(PageQuery pq, String dictName, String dictType) {
        Page<SysDictType> page = pq.toPage();
        LambdaQueryWrapper<SysDictType> w = new LambdaQueryWrapper<>();
        if (dictName != null && !dictName.isBlank()) {
            w.like(SysDictType::getDictName, dictName);
        }
        if (dictType != null && !dictType.isBlank()) {
            w.like(SysDictType::getDictType, dictType);
        }
        w.orderByDesc(SysDictType::getId);
        return PageResult.of(typeMapper.selectPage(page, w));
    }

    public List<SysDictType> listType() {
        return typeMapper.selectList(new LambdaQueryWrapper<SysDictType>().orderByDesc(SysDictType::getId));
    }

    public void createType(SysDictType type) {
        checkTypeUnique(type);
        typeMapper.insert(type);
    }

    public void updateType(SysDictType type) {
        checkTypeUnique(type);
        SysDictType old = typeMapper.selectById(type.getId());
        typeMapper.updateById(type);
        // 类型编码变更，同步更新字典数据
        if (old != null && !old.getDictType().equals(type.getDictType())) {
            SysDictData upd = new SysDictData();
            upd.setDictType(type.getDictType());
            dataMapper.update(upd, new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, old.getDictType()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType type = typeMapper.selectById(id);
        if (type == null) {
            return;
        }
        typeMapper.deleteById(id);
        dataMapper.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, type.getDictType()));
    }

    private void checkTypeUnique(SysDictType type) {
        LambdaQueryWrapper<SysDictType> w = new LambdaQueryWrapper<SysDictType>().eq(SysDictType::getDictType, type.getDictType());
        if (type.getId() != null) {
            w.ne(SysDictType::getId, type.getId());
        }
        if (typeMapper.selectCount(w) > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "字典类型已存在");
        }
    }

    /* ---------- 字典数据 ---------- */

    public List<SysDictData> listData(String dictType) {
        return dataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 0)
                .orderByAsc(SysDictData::getSort));
    }

    public void createData(SysDictData data) {
        dataMapper.insert(data);
    }

    public void updateData(SysDictData data) {
        dataMapper.updateById(data);
    }

    public void deleteData(Long id) {
        dataMapper.deleteById(id);
    }
}
