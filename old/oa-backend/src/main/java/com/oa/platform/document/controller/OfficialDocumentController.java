package com.oa.platform.document.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.document.entity.OfficialDocument;
import com.oa.platform.document.service.OfficialDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公文管理。
 */
@Tag(name = "公文管理")
@RestController
@RequestMapping("/document/official")
public class OfficialDocumentController {

    private final OfficialDocumentService docService;

    public OfficialDocumentController(OfficialDocumentService docService) {
        this.docService = docService;
    }

    @Operation(summary = "公文分页/查询")
    @PreAuthorize("@ss.hasPerm('document:official:list')")
    @GetMapping("/page")
    public R<PageResult<OfficialDocument>> page(PageQuery pq,
                                                @RequestParam(required = false) String docType,
                                                @RequestParam(required = false) String title,
                                                @RequestParam(required = false) String docNo,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Long deptId) {
        return R.ok(docService.page(pq, docType, title, docNo, status, deptId));
    }

    @Operation(summary = "公文详情")
    @GetMapping("/{id}")
    public R<OfficialDocument> detail(@PathVariable Long id) {
        return R.ok(docService.detail(id));
    }

    @Operation(summary = "新建公文(草稿/收文登记)")
    @OperLog(title = "公文管理", businessType = 1)
    @PreAuthorize("@ss.hasPerm('document:official:add')")
    @PostMapping
    public R<Long> create(@RequestBody OfficialDocument doc) {
        return R.ok(docService.create(doc));
    }

    @Operation(summary = "修改公文")
    @OperLog(title = "公文管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('document:official:edit')")
    @PutMapping
    public R<Void> update(@RequestBody OfficialDocument doc) {
        docService.update(doc);
        return R.ok();
    }

    @Operation(summary = "发文提交审批")
    @OperLog(title = "公文管理", businessType = 2)
    @PreAuthorize("@ss.hasPerm('document:official:submit')")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        docService.submit(id);
        return R.ok();
    }

    @Operation(summary = "删除公文")
    @OperLog(title = "公文管理", businessType = 3)
    @PreAuthorize("@ss.hasPerm('document:official:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        docService.delete(id);
        return R.ok();
    }

    @Operation(summary = "公文统计")
    @PreAuthorize("@ss.hasPerm('document:official:list')")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics() {
        return R.ok(docService.statistics());
    }

    @Operation(summary = "生成文号")
    @GetMapping("/gen-doc-no")
    public R<String> genDocNo() {
        return R.ok(docService.generateDocNo());
    }
}
