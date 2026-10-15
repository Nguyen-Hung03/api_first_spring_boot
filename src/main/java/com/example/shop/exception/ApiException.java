package com.example.shop.exception;

import com.example.shop.api.model.FieldErrorDto;
import org.springframework.http.HttpStatus;

import java.util.List;

public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final List<FieldErrorDto> details;

    protected ApiException(HttpStatus status, String code, String message, List<FieldErrorDto> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details == null ? List.of() : List.copyOf(details);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public List<FieldErrorDto> getDetails() {
        return details;
    }

    protected static FieldErrorDto fieldError(String field, String message) {
        return new FieldErrorDto(message).field(field);
    }
}
