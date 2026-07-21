package com.oa.platform.portal.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.document.entity.OfficialDocument;
import com.oa.platform.document.mapper.OfficialDocumentMapper;
import com.oa.platform.portal.entity.Notice;
import com.oa.platform.portal.mapper.NoticeMapper;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 门户首页/工作台汇总服务。
 */
@Service
public class DashboardService {

    private final FlowService flowService;
    private final SysMessageService messageService;
    private final NoticeMapper noticeMapper;
    private final OfficialDocumentMapper docMapper;

    public DashboardService(FlowService flowService, SysMessageService messageService,
                            NoticeMapper noticeMapper, OfficialDocumentMapper docMapper) {
        this.flowService = flowService;
        this.messageService = messageService;
        this.noticeMapper = noticeMapper;
        this.docMapper = docMapper;
    }

    /** 门户首页统计 */
    public Map<String, Object> summary() {
        Map<String, Object> result = new HashMap<>();
        result.put("todoCount", flowService.myTodo().size());
        result.put("doneCount", flowService.myDone().size());
        result.put("unreadMessage", messageService.unreadCount());
        result.put("myDocs", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getDrafterUserId, SecurityUtils.getCurrentUserId())));

        // 最近公告
        List<Notice> notices = noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getTop)
                .orderByDesc(Notice::getPublishTime)
                .last("LIMIT 5"));
        result.put("recentNotices", notices);

        // 最近待办
        result.put("recentTodo", flowService.myTodo().stream().limit(5).toList());
        return result;
    }

    /** 工作台：待办/已办/办结/我的发起数量 */
    public Map<String, Object> workbenchStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("todo", flowService.myTodo().size());
        result.put("done", flowService.myDone().size());
        result.put("mine", flowService.myInitiated().size());
        return result;
    }
}
