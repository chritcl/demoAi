package com.oa.platform.portal.controller;

import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.portal.entity.Article;
import com.oa.platform.portal.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 信息发布。
 */
@Tag(name = "信息发布")
@RestController
@RequestMapping("/portal/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "管理分页")
    @PreAuthorize("@ss.hasPerm('portal:article:list')")
    @GetMapping("/page")
    public R<PageResult<Article>> page(PageQuery pq,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) Integer status,
                                       @RequestParam(required = false) String category) {
        return R.ok(articleService.page(pq, title, status, category));
    }

    @Operation(summary = "已发布文章")
    @GetMapping("/published")
    public R<PageResult<Article>> published(PageQuery pq,
                                            @RequestParam(required = false) String category) {
        return R.ok(articleService.published(pq, category));
    }

    @Operation(summary = "详情(计浏览)")
    @GetMapping("/{id}")
    public R<Article> detail(@PathVariable Long id) {
        return R.ok(articleService.detail(id, true));
    }

    @Operation(summary = "新增")
    @OperLog(title = "信息发布", businessType = 1)
    @PreAuthorize("@ss.hasPerm('portal:article:add')")
    @PostMapping
    public R<Void> create(@RequestBody Article article) {
        articleService.create(article);
        return R.ok();
    }

    @Operation(summary = "修改")
    @OperLog(title = "信息发布", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:article:edit')")
    @PutMapping
    public R<Void> update(@RequestBody Article article) {
        articleService.update(article);
        return R.ok();
    }

    @Operation(summary = "提交审核")
    @OperLog(title = "信息发布", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:article:edit')")
    @PutMapping("/{id}/submit")
    public R<Void> submit(@PathVariable Long id) {
        articleService.submit(id);
        return R.ok();
    }

    @Operation(summary = "审核通过并发布")
    @OperLog(title = "信息发布", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:article:audit')")
    @PutMapping("/{id}/pass")
    public R<Void> pass(@PathVariable Long id, @RequestParam(required = false) String comment) {
        articleService.pass(id, comment);
        return R.ok();
    }

    @Operation(summary = "审核驳回")
    @OperLog(title = "信息发布", businessType = 2)
    @PreAuthorize("@ss.hasPerm('portal:article:audit')")
    @PutMapping("/{id}/reject")
    public R<Void> reject(@PathVariable Long id, @RequestParam(required = false) String comment) {
        articleService.reject(id, comment);
        return R.ok();
    }

    @Operation(summary = "删除")
    @OperLog(title = "信息发布", businessType = 3)
    @PreAuthorize("@ss.hasPerm('portal:article:remove')")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return R.ok();
    }
}
