package com.example.shop.exception;

import com.example.shop.api.model.FieldErrorDto;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Request is well-formed but violates a business rule (HTTP 422). */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String code, String message) {
        this(code, message, List.of());
    }

    public BusinessRuleException(String code, String message, List<FieldErrorDto> details) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message, details);
    }

    public static BusinessRuleException onField(String code, String message, String field, String fieldMessage) {
        return new BusinessRuleException(code, message, List.of(fieldError(field, fieldMessage)));
    }
}
