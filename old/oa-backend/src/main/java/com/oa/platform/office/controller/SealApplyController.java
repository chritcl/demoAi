package com.oa.platform.office.controller;

import com.oa.platform.office.entity.SealApply;
import com.oa.platform.office.service.SealApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用印管理")
@RestController
@RequestMapping("/office/seal")
public class SealApplyController extends AbstractApplyController<SealApply, SealApplyService> {
    private final SealApplyService service;

    public SealApplyController(SealApplyService service) {
        this.service = service;
    }

    @Override
    protected SealApplyService service() {
        return service;
    }

    @Override
    protected String perm() {
        return "office:seal";
    }

    @Override
    protected String title() {
        return "用印申请";
    }
}
