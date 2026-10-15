package com.example.shop.repository;

import com.example.shop.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName, String email, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
