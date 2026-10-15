package com.example.shop.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

final class ApiPaths {

    /** Must match {@code servers[0].url} in shop-api.yaml. */
    static final String BASE = "/api/v1";

    private ApiPaths() {
    }

    /** Builds the {@code Location} header for a resource created by a POST on the current collection URL. */
    static URI locationOf(Long id) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQuery(null)
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
