package com.oa.platform.contacts.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.system.entity.SysDept;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.mapper.SysDeptMapper;
import com.oa.platform.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通讯录服务：组织树 + 搜索（姓名/模糊/拼音）。
 */
@Service
public class ContactsService {

    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    public ContactsService(SysDeptMapper deptMapper, SysUserMapper userMapper) {
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    /** 部门树（含成员，用于树状通讯录） */
    public List<Map<String, Object>> deptTree() {
        List<SysDept> depts = deptMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 0).orderByAsc(SysDept::getSort));
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 0));
        Map<Long, List<SysUser>> usersByDept = users.stream()
                .filter(u -> u.getDeptId() != null)
                .collect(Collectors.groupingBy(SysUser::getDeptId));
        return buildTree(depts, 0L, usersByDept);
    }

    /** 搜索联系人 */
    public List<SysUser> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        String kw = keyword.trim();
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 0)
                .and(i -> i.like(SysUser::getNickname, kw)
                        .or().like(SysUser::getUsername, kw)
                        .or().like(SysUser::getPhone, kw)
                        .or().like(SysUser::getPinyin, kw.toLowerCase())
                        .or().like(SysUser::getEmail, kw))
                .orderByAsc(SysUser::getDeptId);
        List<SysUser> list = userMapper.selectList(w);
        list.forEach(u -> {
            u.setPassword(null);
            u.setPinyin(null);
        });
        return list;
    }

    private List<Map<String, Object>> buildTree(List<SysDept> all, Long parentId, Map<Long, List<SysUser>> usersByDept) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysDept dept : all) {
            if (parentId.equals(dept.getParentId())) {
                Map<String, Object> node = new LinkedHashMap<>();
                node.put("id", dept.getId());
                node.put("parentId", dept.getParentId());
                node.put("label", dept.getDeptName());
                node.put("leader", dept.getLeader());
                node.put("phone", dept.getPhone());
                node.put("type", "dept");
                List<Map<String, Object>> children = buildTree(all, dept.getId(), usersByDept);
                List<SysUser> members = usersByDept.getOrDefault(dept.getId(), Collections.emptyList());
                List<Map<String, Object>> memberNodes = members.stream().map(this::memberNode).collect(Collectors.toList());
                List<Map<String, Object>> allChildren = new ArrayList<>();
                allChildren.addAll(children);
                allChildren.addAll(memberNodes);
                node.put("children", allChildren);
                result.add(node);
            }
        }
        return result;
    }

    private Map<String, Object> memberNode(SysUser u) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", "u_" + u.getId());
        node.put("userId", u.getId());
        node.put("label", u.getNickname());
        node.put("type", "user");
        node.put("phone", u.getPhone());
        node.put("email", u.getEmail());
        node.put("gender", u.getGender());
        node.put("avatar", u.getAvatar());
        return node;
    }
}
