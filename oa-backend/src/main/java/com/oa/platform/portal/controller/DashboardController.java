package com.oa.platform.portal.controller;

import com.oa.platform.common.api.R;
import com.oa.platform.portal.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 门户首页/工作台。
 */
@Tag(name = "门户与工作台")
@RestController
@RequestMapping("/portal/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "门户首页统计")
    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        return R.ok(dashboardService.summary());
    }

    @Operation(summary = "工作台数量统计")
    @GetMapping("/workbench")
    public R<Map<String, Object>> workbench() {
        return R.ok(dashboardService.workbenchStats());
    }
}
