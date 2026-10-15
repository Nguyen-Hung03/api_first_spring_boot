package com.example.shop.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class BadRequestException extends ApiException {

    public BadRequestException(String code, String message, String field, String fieldMessage) {
        super(HttpStatus.BAD_REQUEST, code, message, List.of(fieldError(field, fieldMessage)));
    }
}
