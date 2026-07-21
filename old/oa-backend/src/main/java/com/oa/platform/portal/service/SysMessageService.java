package com.oa.platform.portal.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.portal.entity.SysMessage;
import com.oa.platform.portal.mapper.SysMessageMapper;
import com.oa.platform.system.mapper.SysUserMapper;
import com.oa.platform.system.entity.SysUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 站内消息服务。
 */
@Service
public class SysMessageService {

    private final SysMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    public SysMessageService(SysMessageMapper messageMapper, SysUserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    /** 当前用户消息分页 */
    public PageResult<SysMessage> myPage(PageQuery pq, String type, Integer isRead) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<SysMessage> page = pq.toPage();
        LambdaQueryWrapper<SysMessage> w = new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, userId);
        if (type != null && !type.isBlank()) {
            w.eq(SysMessage::getType, type);
        }
        if (isRead != null) {
            w.eq(SysMessage::getIsRead, isRead);
        }
        w.orderByDesc(SysMessage::getId);
        IPage<SysMessage> result = messageMapper.selectPage(page, w);
        return PageResult.of(result);
    }

    /** 未读数量 */
    public long unreadCount() {
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, SecurityUtils.getCurrentUserId())
                .eq(SysMessage::getIsRead, 0));
    }

    /** 标记已读 */
    public void markRead(Long id) {
        SysMessage msg = new SysMessage();
        msg.setId(id);
        msg.setIsRead(1);
        messageMapper.updateById(msg);
    }

    /** 全部已读 */
    public void markAllRead() {
        SysMessage update = new SysMessage();
        update.setIsRead(1);
        messageMapper.update(update, new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, SecurityUtils.getCurrentUserId())
                .eq(SysMessage::getIsRead, 0));
    }

    /** 发送给指定用户 */
    public void sendToUser(Long userId, String title, String content, String type, String businessType, Long businessId) {
        SysMessage msg = new SysMessage();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setType(type);
        msg.setBusinessType(businessType);
        msg.setBusinessId(businessId);
        msg.setIsRead(0);
        msg.setSendUserId(SecurityUtils.getCurrentUserId());
        msg.setSendUserName(SecurityUtils.getCurrentUsername());
        messageMapper.insert(msg);
    }

    /** 发送给全员 */
    public void sendToAll(String title, String content, String type) {
        List<SysUser> users = userMapper.selectList(null);
        for (SysUser u : users) {
            if (Constants.SUPER_ADMIN_ID.equals(u.getId())) {
                continue;
            }
            sendToUser(u.getId(), title, content, type, null, null);
        }
    }
}
