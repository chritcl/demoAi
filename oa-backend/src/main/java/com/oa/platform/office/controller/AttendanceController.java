package com.oa.platform.office.controller;

import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.R;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.office.entity.Attendance;
import com.oa.platform.office.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 智慧考勤。
 */
@Tag(name = "考勤管理")
@RestController
@RequestMapping("/office/attendance")
public class AttendanceController {

    private final AttendanceService service;

    public AttendanceController(AttendanceService service) {
        this.service = service;
    }

    @Operation(summary = "上班打卡")
    @PostMapping("/clock-in")
    public R<Attendance> clockIn() {
        return R.ok(service.clockIn());
    }

    @Operation(summary = "下班打卡")
    @PostMapping("/clock-out")
    public R<Attendance> clockOut() {
        return R.ok(service.clockOut());
    }

    @Operation(summary = "今日打卡状态")
    @GetMapping("/today")
    public R<Attendance> today() {
        return R.ok(service.today());
    }

    @Operation(summary = "我的考勤记录")
    @GetMapping("/my")
    public R<PageResult<Attendance>> my(PageQuery pq, @RequestParam(required = false) String month) {
        return R.ok(service.myPage(pq, month));
    }

    @Operation(summary = "考勤分页(管理)")
    @PreAuthorize("@ss.hasPerm('office:attendance:list')")
    @GetMapping("/page")
    public R<PageResult<Attendance>> page(PageQuery pq,
                                          @RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) Long deptId,
                                          @RequestParam(required = false) String day) {
        return R.ok(service.page(pq, userId, deptId, day));
    }

    @Operation(summary = "考勤统计")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics() {
        return R.ok(service.statistics());
    }
}
