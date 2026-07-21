package com.oa.platform.portal.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.portal.entity.Notice;
import com.oa.platform.portal.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告。
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/portal/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "管理分页")
    @PreAuthorize("@ss.hasPerm('portal:notice:list')")
    @GetMapping("/page")
    public R<PageResult<Notice>> page(PageQuery pq,
                                      @RequestParam(required = false) String title,
                                      @RequestParam(required = false) Integer status,
                                      @RequestParam(required = false) String category) {
        return R.ok(noticeService.page(pq, title, status, category));
    }

    @Operation(summary = "已发布公告(门户/工作台)")
    @GetMapping("/published")
    public R<PageResult<Notice>> published(PageQuery pq,
                                           @RequestParam(required = false) String title) {
        return R.ok(noticeService.published(pq, title));
    }

    @Operation(summary = "详情(计阅读)")
    @GetMapping("/{id}")
    public R<Notice> detail(@PathVariable Long id) {
        return R.ok(noticeService.detail(id, true));
    }

    @Operation(summary = "新增")
    @OperLog(title = "通知公告", businessType = 1)
    @PreAuthorize("@ss.hasPerm('portal:notice:add')")
    @PostMapping
    public R<Void> create(@RequestBody Notice notice) {
        noticeService.create(notice);
        return R.ok();
    }

    @Operation(summary = "修改")
    @OperLog(title = "通知公告", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:notice:edit')")
    @PutMapping
    public R<Void> update(@RequestBody Notice notice) {
        noticeService.update(notice);
        return R.ok();
    }

    @Operation(summary = "发布")
    @OperLog(title = "通知公告", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:notice:publish')")
    @PutMapping("/{id}/publish")
    public R<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return R.ok();
    }

    @Operation(summary = "撤回")
    @OperLog(title = "通知公告", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:notice:publish')")
    @PutMapping("/{id}/withdraw")
    public R<Void> withdraw(@PathVariable Long id) {
        noticeService.withdraw(id);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(title = "通知公告", businessType = 3)
    @PreAuthorize("@ss.hasPerm('portal:notice:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return R.ok();
    }
}
