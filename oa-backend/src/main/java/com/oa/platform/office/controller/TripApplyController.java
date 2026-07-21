package com.oa.platform.office.controller;

import com.oa.platform.office.entity.TripApply;
import com.oa.platform.office.service.TripApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "出差管理")
@RestController
@RequestMapping("/office/trip")
public class TripApplyController extends AbstractApplyController<TripApply, TripApplyService> {
    private final TripApplyService service;

    public TripApplyController(TripApplyService service) {
        this.service = service;
    }

    @Override
    protected TripApplyService service() {
        return service;
    }

    @Override
    protected String perm() {
        return "office:trip";
    }

    @Override
    protected String title() {
        return "出差申请";
    }
}
