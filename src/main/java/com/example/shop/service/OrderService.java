package com.example.shop.service;

import com.example.shop.api.model.FieldErrorDto;
import com.example.shop.api.model.OrderCreateRequestDto;
import com.example.shop.api.model.OrderItemRequestDto;
import com.example.shop.api.model.OrderPageDto;
import com.example.shop.api.model.OrderResponseDto;
import com.example.shop.api.model.OrderStatusDto;
import com.example.shop.common.PageRequests;
import com.example.shop.entity.Customer;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderStatus;
import com.example.shop.entity.Product;
import com.example.shop.exception.BusinessRuleException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.mapper.ShopMapper;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "totalAmount", "status", "createdAt");

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    public OrderPageDto findAll(Integer page, Integer size, String sort, OrderStatusDto status) {
        Pageable pageable = PageRequests.of(page, size, sort, SORTABLE_FIELDS);
        Page<Order> result = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(ShopMapper.toEntity(status), pageable);
        return ShopMapper.toOrderPage(result);
    }

    public OrderResponseDto findById(Long id) {
        return ShopMapper.toDto(getOrder(id));
    }

    @Transactional
    public OrderResponseDto create(OrderCreateRequestDto request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("khách hàng", request.getCustomerId()));

        Map<Long, Integer> quantities = mergeQuantities(request.getItems());
        Map<Long, Product> products = productRepository.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        for (Long productId : quantities.keySet()) {
            if (!products.containsKey(productId)) {
                throw new ResourceNotFoundException("sản phẩm", productId);
            }
        }

        List<FieldErrorDto> stockErrors = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product.getStockQuantity() < entry.getValue()) {
                stockErrors.add(new FieldErrorDto(
                        "Sản phẩm '%s' (id = %d) chỉ còn %d, yêu cầu %d".formatted(
                                product.getName(), product.getId(), product.getStockQuantity(), entry.getValue()))
                        .field("items.quantity"));
            }
        }
        if (!stockErrors.isEmpty()) {
            throw new BusinessRuleException("INSUFFICIENT_STOCK", "Không đủ hàng tồn kho để đặt hàng", stockErrors);
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setNote(request.getNote());
        order.setShippingAddress(request.getShippingAddress() != null && !request.getShippingAddress().isBlank()
                ? request.getShippingAddress()
                : customer.getAddress());

        quantities.forEach((productId, quantity) -> {
            Product product = products.get(productId);
            product.setStockQuantity(product.getStockQuantity() - quantity);
            order.addItem(new OrderItem(product, quantity));
        });
        order.recalculateTotal();

        return ShopMapper.toDto(orderRepository.saveAndFlush(order));
    }

    @Transactional
    public OrderResponseDto updateStatus(Long id, OrderStatusDto newStatusDto) {
        Order order = getOrder(id);
        OrderStatus current = order.getStatus();
        OrderStatus next = ShopMapper.toEntity(newStatusDto);

        if (current == next) {
            return ShopMapper.toDto(order);
        }
        if (!current.canTransitionTo(next)) {
            throw BusinessRuleException.onField("INVALID_STATUS_TRANSITION",
                    "Không thể chuyển đơn hàng từ %s sang %s".formatted(current, next),
                    "status", "Trạng thái hợp lệ tiếp theo: " + current.allowedNextStatuses());
        }
        if (next == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(next);
        return ShopMapper.toDto(orderRepository.saveAndFlush(order));
    }

    @Transactional
    public void delete(Long id) {
        Order order = getOrder(id);
        switch (order.getStatus()) {
            case PENDING -> restoreStock(order);
            case CANCELLED -> { }
            default -> throw new BusinessRuleException("ORDER_NOT_DELETABLE",
                    "Chỉ được xóa đơn hàng ở trạng thái PENDING hoặc CANCELLED (hiện tại: %s)"
                            .formatted(order.getStatus()));
        }
        orderRepository.delete(order);
    }

    private Order getOrder(Long id) {
        return orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("đơn hàng", id));
    }

    private static void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        }
    }

    private static Map<Long, Integer> mergeQuantities(List<OrderItemRequestDto> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequestDto item : items) {
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }
}
