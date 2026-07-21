package com.oa.platform.office.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.office.entity.Attendance;
import com.oa.platform.office.mapper.AttendanceMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 考勤服务。
 */
@Service
public class AttendanceService {

    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    private final AttendanceMapper mapper;

    public AttendanceService(AttendanceMapper mapper) {
        this.mapper = mapper;
    }

    /** 上班打卡 */
    public Attendance clockIn() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();
        Attendance att = mapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, userId).eq(Attendance::getAttDate, today));
        LocalTime now = LocalTime.now();
        if (att == null) {
            att = new Attendance();
            att.setUserId(userId);
            att.setUserName(SecurityUtils.getLoginUser().getNickname());
            att.setDeptId(SecurityUtils.getCurrentDeptId());
            att.setAttDate(today);
            att.setClockIn(now);
            att.setStatus(now.isAfter(WORK_START) ? "late" : "normal");
            mapper.insert(att);
        } else if (att.getClockIn() == null) {
            Attendance upd = new Attendance();
            upd.setId(att.getId());
            upd.setClockIn(now);
            upd.setStatus(now.isAfter(WORK_START) ? "late" : (att.getClockOut() == null ? "normal" : att.getStatus()));
            mapper.updateById(upd);
            att.setClockIn(now);
        } else {
            throw new BusinessException("今日已打卡上班");
        }
        return att;
    }

    /** 下班打卡 */
    public Attendance clockOut() {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();
        Attendance att = mapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, userId).eq(Attendance::getAttDate, today));
        if (att == null || att.getClockIn() == null) {
            throw new BusinessException("请先完成上班打卡");
        }
        if (att.getClockOut() != null) {
            throw new BusinessException("今日已打卡下班");
        }
        LocalTime now = LocalTime.now();
        Attendance upd = new Attendance();
        upd.setId(att.getId());
        upd.setClockOut(now);
        if (now.isBefore(WORK_END)) {
            upd.setStatus("earlyLeave");
        }
        mapper.updateById(upd);
        att.setClockOut(now);
        if (now.isBefore(WORK_END)) {
            att.setStatus("earlyLeave");
        }
        return att;
    }

    /** 今日状态 */
    public Attendance today() {
        return mapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, SecurityUtils.getCurrentUserId())
                .eq(Attendance::getAttDate, LocalDate.now()));
    }

    public PageResult<Attendance> myPage(PageQuery pq, String month) {
        Page<Attendance> page = pq.toPage();
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getUserId, SecurityUtils.getCurrentUserId());
        if (month != null && !month.isBlank()) {
            w.likeRight(Attendance::getAttDate, month);
        }
        w.orderByDesc(Attendance::getAttDate);
        return PageResult.of(mapper.selectPage(page, w));
    }

    public PageResult<Attendance> page(PageQuery pq, Long userId, Long deptId, String day) {
        Page<Attendance> page = pq.toPage();
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<>();
        if (userId != null) {
            w.eq(Attendance::getUserId, userId);
        }
        if (deptId != null) {
            w.eq(Attendance::getDeptId, deptId);
        }
        if (day != null && !day.isBlank()) {
            w.eq(Attendance::getAttDate, LocalDate.parse(day));
        }
        w.orderByDesc(Attendance::getAttDate);
        return PageResult.of(mapper.selectPage(page, w));
    }

    public Map<String, Object> statistics() {
        Map<String, Object> result = new HashMap<>();
        result.put("normal", count("normal"));
        result.put("late", count("late"));
        result.put("earlyLeave", count("earlyLeave"));
        result.put("absent", count("absent"));
        return result;
    }

    private Long count(String status) {
        return mapper.selectCount(new LambdaQueryWrapper<Attendance>().eq(Attendance::getStatus, status));
    }
}
