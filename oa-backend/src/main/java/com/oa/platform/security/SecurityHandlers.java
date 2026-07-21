package com.oa.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.platform.common.api.R;
import com.oa.platform.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 安全相关 JSON 响应处理器（401 / 403）。
 */
@Component
public class SecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, R.fail(ResultCode.UNAUTHORIZED));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        writeJson(response, HttpServletResponse.SC_FORBIDDEN, R.fail(ResultCode.FORBIDDEN));
    }

    private void writeJson(HttpServletResponse response, int status, R<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
