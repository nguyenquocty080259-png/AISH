package com.aish.mvc.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Case "đã đăng nhập nhưng SAI ROLE" (vd: USER gọi /api/admin/**) là một
 * AccessDeniedException bị Spring Security chặn NGAY TẠI filter chain — trước
 * DispatcherServlet — nên GlobalExceptionHandler không thấy được, giống hệt lý do cần
 * RestAuthenticationEntryPoint cho case "chưa đăng nhập". Không có handler riêng, case
 * này rơi vào forward "/error" mặc định (không nằm trong permitAll) -> lại bị chặn thành
 * 401 sai, dù đáng lẽ phải là 403. Handler này đảm bảo case đó cũng trả đúng 403 +
 * cùng hình dạng JSON như mọi lỗi khác trong app.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), 403, "Forbidden", "Bạn không có quyền thực hiện hành động này.", request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
