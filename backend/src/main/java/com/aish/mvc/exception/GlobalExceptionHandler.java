package com.aish.mvc.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Bắt lỗi tập trung cho toàn app. Trước đây KHÔNG có @RestControllerAdvice, nên bất kỳ
 * exception nào thoát khỏi controller khiến Spring Boot forward sang "/error" — nhưng
 * "/error" không nằm trong danh sách permitAll của SecurityConfig, nên request đó lại bị
 * chặn và trả về 401 thay vì mã lỗi thật sự (403/404/500). Handler này chặn exception
 * NGAY TẠI tầng DispatcherServlet, trước khi có forward "/error" nào xảy ra, nên loại bỏ
 * hẳn hiện tượng "401 giả" đó — mọi lỗi giờ trả đúng mã + JSON nhất quán.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ AccessDeniedException.class, ForbiddenException.class })
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Dữ liệu không hợp lệ." : message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message.isBlank() ? "Dữ liệu không hợp lệ." : message, request);
    }

    // Không phải @Valid, nhưng cùng lớp "client gửi sai/thiếu điều kiện" -> cũng 400,
    // thay vì rơi vào 500 chung chung (ví dụ: guard "chỉ appeal được doc REJECTED").
    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // Nhiều service (CollectionServiceImpl, ...) ném ResponseStatusException để tự set status +
    // message tiếng Việt (409 trùng tên, 400 rỗng, 404 không sở hữu...). Không có handler riêng thì
    // nó rơi vào handleUnexpected(Exception) bên dưới -> mất status thật lẫn message, chỉ còn 500 chung chung.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Yêu cầu đăng nhập.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không mong muốn tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Đã có lỗi xảy ra, vui lòng thử lại sau.", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
