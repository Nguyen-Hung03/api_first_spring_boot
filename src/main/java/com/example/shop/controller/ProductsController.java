package com.example.shop.controller;

import com.example.shop.api.ProductsApi;
import com.example.shop.api.model.ProductPageDto;
import com.example.shop.api.model.ProductPatchRequestDto;
import com.example.shop.api.model.ProductRequestDto;
import com.example.shop.api.model.ProductResponseDto;
import com.example.shop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE)
public class ProductsController implements ProductsApi {

    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<ProductPageDto> listProducts(Integer page, Integer size, String sort,
                                                       String keyword, String category) {
        return ResponseEntity.ok(productService.findAll(page, size, sort, keyword, category));
    }

    @Override
    public ResponseEntity<ProductResponseDto> createProduct(ProductRequestDto productRequestDto) {
        ProductResponseDto created = productService.create(productRequestDto);
        return ResponseEntity.created(ApiPaths.locationOf(created.getId())).body(created);
    }

    @Override
    public ResponseEntity<ProductResponseDto> getProductById(Long productId) {
        return ResponseEntity.ok(productService.findById(productId));
    }

    @Override
    public ResponseEntity<ProductResponseDto> replaceProduct(Long productId, ProductRequestDto productRequestDto) {
        return ResponseEntity.ok(productService.replace(productId, productRequestDto));
    }

    @Override
    public ResponseEntity<ProductResponseDto> updateProduct(Long productId,
                                                            ProductPatchRequestDto productPatchRequestDto) {
        return ResponseEntity.ok(productService.update(productId, productPatchRequestDto));
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
