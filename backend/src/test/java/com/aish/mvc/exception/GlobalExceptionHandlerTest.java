package com.aish.mvc.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    // Trước đây DataIntegrityViolationException (vd. username trùng do race) không có handler
    // riêng nên rơi vào handleUnexpected() -> 500 chung chung. Giờ phải trả 409 kèm thông báo
    // tiếng Việt rõ ràng để FE phân biệt được với lỗi hệ thống thật sự.
    @Test
    void dataIntegrityViolationMapsTo409WithVietnameseMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/profile/onboarding");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value"), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Dữ liệu bị trùng, vui lòng thử lại.", response.getBody().getMessage());
    }
}
