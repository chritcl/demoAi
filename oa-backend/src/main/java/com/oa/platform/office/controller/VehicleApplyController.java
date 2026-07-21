package com.oa.platform.office.controller;

import com.oa.platform.office.entity.VehicleApply;
import com.oa.platform.office.service.VehicleApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用车管理")
@RestController
@RequestMapping("/office/vehicle")
public class VehicleApplyController extends AbstractApplyController<VehicleApply, VehicleApplyService> {
    @Override
    protected VehicleApplyService service() {
        return service;
    }

    @Override
    protected String perm() {
        return "office:vehicle";
    }

    @Override
    protected String title() {
        return "用车申请";
    }

    private final VehicleApplyService service;

    public VehicleApplyController(VehicleApplyService service) {
        this.service = service;
    }
}
