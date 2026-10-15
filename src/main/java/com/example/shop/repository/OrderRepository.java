package com.example.shop.repository;

import com.example.shop.entity.Order;
import com.example.shop.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = "customer")
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "customer")
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = {"customer", "items"})
    Optional<Order> findWithDetailsById(Long id);

    boolean existsByCustomerId(Long customerId);
}
