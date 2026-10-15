package com.example.shop.controller;

import com.example.shop.api.OrdersApi;
import com.example.shop.api.model.OrderCreateRequestDto;
import com.example.shop.api.model.OrderPageDto;
import com.example.shop.api.model.OrderResponseDto;
import com.example.shop.api.model.OrderStatusDto;
import com.example.shop.api.model.OrderStatusUpdateRequestDto;
import com.example.shop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE)
public class OrdersController implements OrdersApi {

    private final OrderService orderService;

    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderPageDto> listOrders(Integer page, Integer size, String sort, OrderStatusDto status) {
        return ResponseEntity.ok(orderService.findAll(page, size, sort, status));
    }

    @Override
    public ResponseEntity<OrderResponseDto> createOrder(OrderCreateRequestDto orderCreateRequestDto) {
        OrderResponseDto created = orderService.create(orderCreateRequestDto);
        return ResponseEntity.created(ApiPaths.locationOf(created.getId())).body(created);
    }

    @Override
    public ResponseEntity<OrderResponseDto> getOrderById(Long orderId) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    @Override
    public ResponseEntity<OrderResponseDto> updateOrderStatus(Long orderId,
                                                              OrderStatusUpdateRequestDto orderStatusUpdateRequestDto) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, orderStatusUpdateRequestDto.getStatus()));
    }

    @Override
    public ResponseEntity<Void> deleteOrder(Long orderId) {
        orderService.delete(orderId);
        return ResponseEntity.noContent().build();
    }
}
