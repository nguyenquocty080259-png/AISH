package com.aish.mvc.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource());

    private static ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
        ms.setBasename("messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }

    @AfterEach
    void resetLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    private ResponseEntity<ErrorResponse> handleDuplicate() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/profile/onboarding");
        return handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value"), request);
    }

    // DataIntegrityViolationException (vd. username trùng do race) trả 409 kèm thông báo rõ ràng
    // để FE phân biệt với lỗi hệ thống thật sự; thông báo được tra theo ngôn ngữ Accept-Language.
    @Test
    void dataIntegrityViolationMapsTo409WithVietnameseMessage() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("vi"));

        ResponseEntity<ErrorResponse> response = handleDuplicate();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Dữ liệu bị trùng, vui lòng thử lại.", response.getBody().getMessage());
    }

    @Test
    void dataIntegrityViolationMessageIsLocalizedToEnglish() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        ResponseEntity<ErrorResponse> response = handleDuplicate();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Data conflict, please try again.", response.getBody().getMessage());
    }
}
