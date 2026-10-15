package com.example.shop.mapper;

import com.example.shop.api.model.CustomerPageDto;
import com.example.shop.api.model.CustomerResponseDto;
import com.example.shop.api.model.OrderItemResponseDto;
import com.example.shop.api.model.OrderPageDto;
import com.example.shop.api.model.OrderResponseDto;
import com.example.shop.api.model.OrderStatusDto;
import com.example.shop.api.model.ProductPageDto;
import com.example.shop.api.model.ProductResponseDto;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class ShopMapper {

    private ShopMapper() {
    }

    public static CustomerResponseDto toDto(Customer customer) {
        return new CustomerResponseDto(customer.getId(), customer.getFullName(), customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(toOffset(customer.getCreatedAt()))
                .updatedAt(toOffset(customer.getUpdatedAt()));
    }

    public static ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity())
                .description(product.getDescription())
                .category(product.getCategory())
                .createdAt(toOffset(product.getCreatedAt()))
                .updatedAt(toOffset(product.getUpdatedAt()));
    }

    public static OrderResponseDto toDto(Order order) {
        Customer customer = order.getCustomer();
        return new OrderResponseDto(
                order.getId(),
                customer.getId(),
                toDto(order.getStatus()),
                order.getTotalAmount(),
                order.getItems().stream().map(ShopMapper::toDto).toList())
                .customerName(customer.getFullName())
                .shippingAddress(order.getShippingAddress())
                .note(order.getNote())
                .createdAt(toOffset(order.getCreatedAt()))
                .updatedAt(toOffset(order.getUpdatedAt()));
    }

    public static OrderItemResponseDto toDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineTotal());
    }

    public static CustomerPageDto toCustomerPage(Page<Customer> page) {
        return new CustomerPageDto(page.map(ShopMapper::toDto).getContent(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public static ProductPageDto toProductPage(Page<Product> page) {
        return new ProductPageDto(page.map(ShopMapper::toDto).getContent(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public static OrderPageDto toOrderPage(Page<Order> page) {
        return new OrderPageDto(page.map(ShopMapper::toDto).getContent(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public static OrderStatusDto toDto(OrderStatus status) {
        return OrderStatusDto.valueOf(status.name());
    }

    public static OrderStatus toEntity(OrderStatusDto status) {
        return OrderStatus.valueOf(status.name());
    }

    private static OffsetDateTime toOffset(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
