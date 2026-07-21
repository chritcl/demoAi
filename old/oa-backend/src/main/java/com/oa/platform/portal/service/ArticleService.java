package com.oa.platform.portal.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.portal.entity.Article;
import com.oa.platform.portal.mapper.ArticleMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 信息发布服务。
 */
@Service
public class ArticleService {

    private final ArticleMapper articleMapper;

    public ArticleService(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    public PageResult<Article> page(PageQuery pq, String title, Integer status, String category) {
        Page<Article> page = pq.toPage();
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) {
            w.like(Article::getTitle, title);
        }
        if (status != null) {
            w.eq(Article::getStatus, status);
        }
        if (category != null && !category.isBlank()) {
            w.eq(Article::getCategory, category);
        }
        w.orderByDesc(Article::getTop).orderByDesc(Article::getId);
        return PageResult.of(articleMapper.selectPage(page, w));
    }

    public PageResult<Article> published(PageQuery pq, String category) {
        Page<Article> page = pq.toPage();
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 2);
        if (category != null && !category.isBlank()) {
            w.eq(Article::getCategory, category);
        }
        w.orderByDesc(Article::getTop).orderByDesc(Article::getPublishTime);
        return PageResult.of(articleMapper.selectPage(page, w));
    }

    public Article detail(Long id, boolean view) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (view) {
            Article upd = new Article();
            upd.setId(id);
            upd.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
            articleMapper.updateById(upd);
        }
        return article;
    }

    public void create(Article article) {
        if (article.getStatus() == null) {
            article.setStatus(0);
        }
        article.setViewCount(0);
        article.setPublishUserId(SecurityUtils.getCurrentUserId());
        if (article.getAuthor() == null) {
            article.setAuthor(SecurityUtils.getLoginUser().getNickname());
        }
        articleMapper.insert(article);
    }

    public void update(Article article) {
        articleMapper.updateById(article);
    }

    /** 提交审核 */
    public void submit(Long id) {
        changeStatus(id, 1, null);
    }

    /** 审核通过并发布 */
    public void pass(Long id, String comment) {
        Article upd = new Article();
        upd.setId(id);
        upd.setStatus(2);
        upd.setAuditComment(comment);
        upd.setPublishUserId(SecurityUtils.getCurrentUserId());
        upd.setPublishTime(LocalDateTime.now());
        articleMapper.updateById(upd);
    }

    /** 驳回 */
    public void reject(Long id, String comment) {
        changeStatus(id, 3, comment);
    }

    public void delete(Long id) {
        articleMapper.deleteById(id);
    }

    private void changeStatus(Long id, Integer status, String comment) {
        Article upd = new Article();
        upd.setId(id);
        upd.setStatus(status);
        upd.setAuditComment(comment);
        articleMapper.updateById(upd);
    }
}
