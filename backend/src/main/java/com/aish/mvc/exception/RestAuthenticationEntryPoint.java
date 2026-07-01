package com.aish.mvc.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Trường hợp "chưa đăng nhập" (không có/token JWT sai) bị Spring Security chặn NGAY TẠI
 * filter chain — trước khi tới DispatcherServlet — nên GlobalExceptionHandler không thấy
 * được. Entry point này đảm bảo case đó cũng trả về ĐÚNG hình dạng JSON như mọi lỗi khác
 * trong app, thay vì một response 401 trống không có body.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Instance riêng — không dùng bean Jackson của Spring (cùng lý do như trong
    // DocEmbeddingServiceImpl: Boot 4 ở đây autoconfigure tools.jackson.JsonMapper,
    // không phải com.fasterxml ObjectMapper cổ điển).
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), 401, "Unauthorized", "Yêu cầu đăng nhập.", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
