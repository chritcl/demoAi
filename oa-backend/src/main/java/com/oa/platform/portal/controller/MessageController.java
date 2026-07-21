package com.oa.platform.portal.controller;

import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.portal.entity.SysMessage;
import com.oa.platform.portal.service.SysMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 站内消息。
 */
@Tag(name = "站内消息")
@RestController
@RequestMapping("/portal/message")
public class MessageController {

    private final SysMessageService messageService;

    public MessageController(SysMessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "我的消息分页")
    @GetMapping("/page")
    public R<PageResult<SysMessage>> page(PageQuery pq,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) Integer isRead) {
        return R.ok(messageService.myPage(pq, type, isRead));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public R<Long> unreadCount() {
        return R.ok(messageService.unreadCount());
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        messageService.markRead(id);
        return R.ok();
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public R<Void> markAllRead() {
        messageService.markAllRead();
        return R.ok();
    }
}
