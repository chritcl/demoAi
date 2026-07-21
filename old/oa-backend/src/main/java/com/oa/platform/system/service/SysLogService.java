package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysOperLog;
import com.oa.platform.system.mapper.SysOperLogMapper;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务。
 */
@Service
public class SysLogService {

    private final SysOperLogMapper logMapper;

    public SysLogService(SysOperLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    public PageResult<SysOperLog> page(PageQuery pq, String title, String operName, Integer status) {
        Page<SysOperLog> page = pq.toPage();
        LambdaQueryWrapper<SysOperLog> w = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) {
            w.like(SysOperLog::getTitle, title);
        }
        if (operName != null && !operName.isBlank()) {
            w.like(SysOperLog::getOperName, operName);
        }
        if (status != null) {
            w.eq(SysOperLog::getStatus, status);
        }
        w.orderByDesc(SysOperLog::getId);
        return PageResult.of(logMapper.selectPage(page, w));
    }

    public void clear() {
        logMapper.delete(new LambdaQueryWrapper<>());
    }

    public void delete(Long id) {
        logMapper.deleteById(id);
    }

    public void save(SysOperLog log) {
        logMapper.insert(log);
    }
}
