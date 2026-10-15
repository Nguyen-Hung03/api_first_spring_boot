package com.example.shop.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                "Không tìm thấy %s với id = %s".formatted(resourceName, id), List.of());
    }
}
