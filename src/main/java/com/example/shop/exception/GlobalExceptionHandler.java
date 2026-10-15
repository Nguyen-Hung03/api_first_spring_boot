package com.example.shop.exception;

import com.example.shop.api.model.ErrorResponseDto;
import com.example.shop.api.model.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/** Converts every error into the single {@code ErrorResponse} shape declared in shop-api.yaml. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String VALIDATION_MESSAGE = "Dữ liệu đầu vào không hợp lệ";

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponseDto> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), ex.getDetails(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBodyValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        List<FieldErrorDto> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(new FieldErrorDto(error.getDefaultMessage()).field(error.getField()));
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> details.add(new FieldErrorDto(error.getDefaultMessage())));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", VALIDATION_MESSAGE, details, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleParameterValidation(HandlerMethodValidationException ex,
                                                                      HttpServletRequest request) {
        List<FieldErrorDto> details = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                details.add(new FieldErrorDto(error.getDefaultMessage()).field(name));
            }
        });
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", VALIDATION_MESSAGE, details, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        List<FieldErrorDto> details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.substring(path.lastIndexOf('.') + 1);
                    return new FieldErrorDto(violation.getMessage()).field(field);
                })
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", VALIDATION_MESSAGE, details, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        FieldErrorDto detail = new FieldErrorDto("Giá trị '%s' không hợp lệ".formatted(ex.getValue()))
                .field(ex.getName());
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", VALIDATION_MESSAGE, List.of(detail), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                   HttpServletRequest request) {
        FieldErrorDto detail = new FieldErrorDto("Tham số bắt buộc").field(ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", VALIDATION_MESSAGE, List.of(detail), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON",
                "Body JSON không hợp lệ hoặc sai kiểu dữ liệu", List.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResource(NoResourceFoundException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "Không tồn tại endpoint " + request.getRequestURI(), List.of(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Phương thức %s không được hỗ trợ cho endpoint này".formatted(ex.getMethod()), List.of(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type phải là application/json", List.of(), request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "Dữ liệu vừa được cập nhật bởi một yêu cầu khác, vui lòng thử lại", List.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Data integrity violation on {}", request.getRequestURI(), ex);
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "DATA_INTEGRITY_VIOLATION",
                "Dữ liệu vi phạm ràng buộc toàn vẹn (trùng lặp hoặc đang được tham chiếu)", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Đã xảy ra lỗi không mong muốn", List.of(), request);
    }

    private static ResponseEntity<ErrorResponseDto> build(HttpStatus status, String code, String message,
                                                          List<FieldErrorDto> details, HttpServletRequest request) {
        ErrorResponseDto body = new ErrorResponseDto(status.value(), code, message, new ArrayList<>(details))
                .path(request.getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return ResponseEntity.status(status).body(body);
    }
}
