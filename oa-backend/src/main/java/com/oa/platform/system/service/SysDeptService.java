package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.system.entity.SysDept;
import com.oa.platform.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门/组织架构服务。
 */
@Service
public class SysDeptService {

    private final SysDeptMapper deptMapper;

    public SysDeptService(SysDeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    /** 列表（按名称过滤后构建树） */
    public List<SysDept> tree(String deptName, Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysDept::getStatus, status);
        }
        wrapper.orderByAsc(SysDept::getSort);
        List<SysDept> all = deptMapper.selectList(wrapper);
        if (deptName != null && !deptName.isBlank()) {
            // 名称过滤：返回匹配项（不构建树，前端平铺展示）
            return all.stream()
                    .filter(d -> d.getDeptName() != null && d.getDeptName().contains(deptName))
                    .collect(Collectors.toList());
        }
        return buildTree(all, 0L);
    }

    public SysDept detail(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return dept;
    }

    public void create(SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        if (dept.getParentId() != 0L) {
            SysDept parent = deptMapper.selectById(dept.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "上级部门不存在");
            }
            dept.setAncestors((parent.getAncestors() == null ? "" : parent.getAncestors()) + "," + parent.getId());
        } else {
            dept.setAncestors("0");
        }
        deptMapper.insert(dept);
    }

    public void update(SysDept dept) {
        if (dept.getId() != null && dept.getId().equals(dept.getParentId())) {
            throw new BusinessException("上级部门不能选择自己");
        }
        SysDept old = deptMapper.selectById(dept.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (dept.getParentId() != null && !dept.getParentId().equals(old.getParentId())) {
            if (dept.getParentId() == 0L) {
                dept.setAncestors("0");
            } else {
                SysDept parent = deptMapper.selectById(dept.getParentId());
                if (parent == null) {
                    throw new BusinessException(ResultCode.DATA_NOT_EXISTS, "上级部门不存在");
                }
                dept.setAncestors((parent.getAncestors() == null ? "" : parent.getAncestors()) + "," + parent.getId());
            }
            // 更新子部门 ancestors
            updateChildrenAncestors(dept.getId(), old.getAncestors(), dept.getAncestors());
        }
        deptMapper.updateById(dept);
    }

    public void delete(Long id) {
        Long childCount = deptMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("存在子部门，不允许删除");
        }
        deptMapper.deleteById(id);
    }

    private void updateChildrenAncestors(Long parentId, String oldAncestors, String newAncestors) {
        List<SysDept> children = deptMapper.selectList(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, parentId));
        for (SysDept child : children) {
            String ancestors = newAncestors + "," + parentId;
            child.setAncestors(ancestors);
            deptMapper.updateById(child);
            updateChildrenAncestors(child.getId(), oldAncestors, ancestors);
        }
    }

    private List<SysDept> buildTree(List<SysDept> all, Long parentId) {
        return all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .sorted(Comparator.comparingInt(d -> d.getSort() == null ? 0 : d.getSort()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
