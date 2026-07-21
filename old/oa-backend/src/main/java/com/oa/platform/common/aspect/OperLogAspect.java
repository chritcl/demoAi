package com.oa.platform.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.platform.common.annotation.OperLog;
import com.oa.platform.common.util.IpUtils;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.system.entity.SysOperLog;
import com.oa.platform.system.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 操作日志切面：拦截 @OperLog 注解并异步记录。
 */
@Aspect
@Component
public class OperLogAspect {

    private final SysLogService logService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperLogAspect(SysLogService logService) {
        this.logService = logService;
    }

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog record = new SysOperLog();
        record.setTitle(operLog.title());
        record.setBusinessType(operLog.businessType());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        record.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        record.setRequestMethod(currentRequestMethod());

        fillRequestInfo(record);
        record.setOperId(SecurityUtils.getCurrentUserId());
        record.setOperName(SecurityUtils.getCurrentUsername());
        record.setOperParam(toParam(joinPoint.getArgs()));

        Object result;
        try {
            result = joinPoint.proceed();
            record.setStatus(0);
            record.setJsonResult(toJson(result));
            return result;
        } catch (Throwable e) {
            record.setStatus(1);
            record.setErrorMsg(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            throw e;
        } finally {
            record.setCostTime(System.currentTimeMillis() - start);
            record.setOperTime(java.time.LocalDateTime.now());
            save(record);
        }
    }

    @Async
    public void save(SysOperLog log) {
        try {
            logService.save(log);
        } catch (Exception ignored) {
            // 日志记录失败不影响主流程
        }
    }

    private void fillRequestInfo(SysOperLog record) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            record.setOperUrl(request.getRequestURI());
            record.setOperIp(IpUtils.getClientIp(request));
        }
    }

    private String currentRequestMethod() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? "UNKNOWN" : attrs.getRequest().getMethod();
    }

    private String toParam(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object[] safe = Arrays.stream(args)
                .filter(a -> !(a instanceof HttpServletRequest) && !(a instanceof MultipartFile))
                .toArray();
        return toJson(safe);
    }

    private String toJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            return null;
        }
    }
}
