package com.oa.platform.portal.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.portal.entity.Notice;
import com.oa.platform.portal.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 通知公告服务。
 */
@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public PageResult<Notice> page(PageQuery pq, String title, Integer status, String category) {
        Page<Notice> page = pq.toPage();
        LambdaQueryWrapper<Notice> w = new LambdaQueryWrapper<>();
        if (title != null && !title.isBlank()) {
            w.like(Notice::getTitle, title);
        }
        if (status != null) {
            w.eq(Notice::getStatus, status);
        }
        if (category != null && !category.isBlank()) {
            w.eq(Notice::getCategory, category);
        }
        w.orderByDesc(Notice::getTop).orderByDesc(Notice::getId);
        return PageResult.of(noticeMapper.selectPage(page, w));
    }

    /** 已发布公告（门户展示） */
    public PageResult<Notice> published(PageQuery pq, String title) {
        Page<Notice> page = pq.toPage();
        LambdaQueryWrapper<Notice> w = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1);
        if (title != null && !title.isBlank()) {
            w.like(Notice::getTitle, title);
        }
        w.orderByDesc(Notice::getTop).orderByDesc(Notice::getPublishTime);
        return PageResult.of(noticeMapper.selectPage(page, w));
    }

    public Notice detail(Long id, boolean incrementRead) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (incrementRead) {
            Notice upd = new Notice();
            upd.setId(id);
            upd.setReadCount((notice.getReadCount() == null ? 0 : notice.getReadCount()) + 1);
            noticeMapper.updateById(upd);
        }
        return notice;
    }

    public void create(Notice notice) {
        if (notice.getStatus() == null) {
            notice.setStatus(0);
        }
        notice.setReadCount(0);
        if (notice.getStatus() == 1) {
            publishFields(notice);
        }
        noticeMapper.insert(notice);
    }

    public void update(Notice notice) {
        if (notice.getStatus() != null && notice.getStatus() == 1) {
            publishFields(notice);
        }
        noticeMapper.updateById(notice);
    }

    public void publish(Long id) {
        Notice upd = new Notice();
        upd.setId(id);
        upd.setStatus(1);
        publishFields(upd);
        noticeMapper.updateById(upd);
    }

    public void withdraw(Long id) {
        Notice upd = new Notice();
        upd.setId(id);
        upd.setStatus(2);
        noticeMapper.updateById(upd);
    }

    public void delete(Long id) {
        noticeMapper.deleteById(id);
    }

    private void publishFields(Notice notice) {
        notice.setPublishTime(LocalDateTime.now());
        notice.setPublishUserId(SecurityUtils.getCurrentUserId());
        notice.setPublishUserName(SecurityUtils.getLoginUser().getNickname());
    }
}
