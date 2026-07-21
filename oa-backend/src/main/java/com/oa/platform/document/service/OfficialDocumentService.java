package com.oa.platform.document.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.document.entity.OfficialDocument;
import com.oa.platform.document.mapper.OfficialDocumentMapper;
import com.oa.platform.workflow.event.FlowCompletedEvent;
import com.oa.platform.workflow.service.FlowService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 公文管理服务。
 */
@Service
public class OfficialDocumentService {

    public static final String BUSINESS_TYPE = "document_send";

    private final OfficialDocumentMapper docMapper;
    private final FlowService flowService;

    public OfficialDocumentService(OfficialDocumentMapper docMapper, FlowService flowService) {
        this.docMapper = docMapper;
        this.flowService = flowService;
    }

    public PageResult<OfficialDocument> page(PageQuery pq, String docType, String title,
                                             String docNo, Integer status, Long deptId) {
        Page<OfficialDocument> page = pq.toPage();
        LambdaQueryWrapper<OfficialDocument> w = new LambdaQueryWrapper<>();
        if (docType != null && !docType.isBlank()) {
            w.eq(OfficialDocument::getDocType, docType);
        }
        if (title != null && !title.isBlank()) {
            w.like(OfficialDocument::getTitle, title);
        }
        if (docNo != null && !docNo.isBlank()) {
            w.like(OfficialDocument::getDocNo, docNo);
        }
        if (status != null) {
            w.eq(OfficialDocument::getStatus, status);
        }
        if (deptId != null) {
            w.eq(OfficialDocument::getDeptId, deptId);
        }
        w.orderByDesc(OfficialDocument::getId);
        return PageResult.of(docMapper.selectPage(page, w));
    }

    public OfficialDocument detail(Long id) {
        OfficialDocument doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        return doc;
    }

    /** 新建（草稿）：发文/收文登记 */
    public Long create(OfficialDocument doc) {
        doc.setDrafterUserId(SecurityUtils.getCurrentUserId());
        doc.setDrafterName(SecurityUtils.getLoginUser().getNickname());
        doc.setDeptId(SecurityUtils.getCurrentDeptId());
        if ("receive".equals(doc.getDocType())) {
            // 收文直接登记完成
            doc.setStatus(1);
        } else if (doc.getStatus() == null) {
            doc.setStatus(0);
        }
        docMapper.insert(doc);
        return doc.getId();
    }

    public void update(OfficialDocument doc) {
        docMapper.updateById(doc);
    }

    public void delete(Long id) {
        docMapper.deleteById(id);
    }

    /** 发文提交审批 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id) {
        OfficialDocument doc = mustOwn(id);
        if (doc.getStatus() != null && doc.getStatus() != 0) {
            throw new BusinessException("当前状态不可提交");
        }
        doc.setStatus(1);
        docMapper.updateById(doc);
        flowService.start(BUSINESS_TYPE, id, "发文审批：" + doc.getTitle());
    }

    /** 文号生成：当年发文流水号 */
    public String generateDocNo() {
        int year = LocalDate.now().getYear();
        Long count = docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getDocType, "send")
                .ge(OfficialDocument::getPublishDate, LocalDate.of(year, 1, 1)));
        return "OA发〔" + year + "〕" + String.format("%04d", (count % 10000) + 1) + "号";
    }

    /** 统计：按类型、状态汇总 */
    public Map<String, Object> statistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("sendTotal", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getDocType, "send")));
        result.put("receiveTotal", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getDocType, "receive")));
        result.put("draft", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getStatus, 0)));
        result.put("processing", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getStatus, 1)));
        result.put("published", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getStatus, 2)));
        result.put("rejected", docMapper.selectCount(new LambdaQueryWrapper<OfficialDocument>()
                .eq(OfficialDocument::getStatus, 3)));
        return result;
    }

    /** 监听流程完成，更新公文状态 */
    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onFlowCompleted(FlowCompletedEvent event) {
        if (!BUSINESS_TYPE.equals(event.getBusinessType())) {
            return;
        }
        OfficialDocument doc = docMapper.selectById(event.getBusinessId());
        if (doc == null) {
            return;
        }
        OfficialDocument upd = new OfficialDocument();
        upd.setId(doc.getId());
        if (event.isApproved()) {
            upd.setStatus(2);
            upd.setPublishDate(LocalDate.now());
            if (doc.getDocNo() == null || doc.getDocNo().isBlank()) {
                upd.setDocNo(generateDocNo());
            }
        } else {
            upd.setStatus(3);
        }
        docMapper.updateById(upd);
    }

    private OfficialDocument mustOwn(Long id) {
        OfficialDocument doc = docMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        if (!SecurityUtils.isAdmin() && !SecurityUtils.getCurrentUserId().equals(doc.getDrafterUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return doc;
    }
}
