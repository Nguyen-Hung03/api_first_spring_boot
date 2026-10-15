package com.example.shop.service;

import com.example.shop.api.model.ProductPageDto;
import com.example.shop.api.model.ProductPatchRequestDto;
import com.example.shop.api.model.ProductRequestDto;
import com.example.shop.api.model.ProductResponseDto;
import com.example.shop.common.PageRequests;
import com.example.shop.entity.Product;
import com.example.shop.exception.BusinessRuleException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.mapper.ShopMapper;
import com.example.shop.repository.OrderItemRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "name", "price", "stockQuantity", "category", "createdAt");

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public ProductPageDto findAll(Integer page, Integer size, String sort, String keyword, String category) {
        Pageable pageable = PageRequests.of(page, size, sort, SORTABLE_FIELDS);
        return ShopMapper.toProductPage(
                productRepository.search(blankToNull(keyword), blankToNull(category), pageable));
    }

    public ProductResponseDto findById(Long id) {
        return ShopMapper.toDto(getProduct(id));
    }

    @Transactional
    public ProductResponseDto create(ProductRequestDto request) {
        Product product = new Product();
        applyFull(product, request);
        return ShopMapper.toDto(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponseDto replace(Long id, ProductRequestDto request) {
        Product product = getProduct(id);
        applyFull(product, request);
        return ShopMapper.toDto(productRepository.saveAndFlush(product));
    }

    @Transactional
    public ProductResponseDto update(Long id, ProductPatchRequestDto request) {
        Product product = getProduct(id);
        if (request.getName() != null) {
            product.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        return ShopMapper.toDto(productRepository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new BusinessRuleException("PRODUCT_IN_USE",
                    "Không thể xóa sản phẩm id = %d vì đã nằm trong đơn hàng".formatted(id));
        }
        productRepository.delete(product);
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", id));
    }

    private static void applyFull(Product product, ProductRequestDto request) {
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
