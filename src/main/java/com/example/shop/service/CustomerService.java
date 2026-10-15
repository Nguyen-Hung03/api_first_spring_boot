package com.example.shop.service;

import com.example.shop.api.model.CustomerPageDto;
import com.example.shop.api.model.CustomerPatchRequestDto;
import com.example.shop.api.model.CustomerRequestDto;
import com.example.shop.api.model.CustomerResponseDto;
import com.example.shop.api.model.OrderPageDto;
import com.example.shop.common.PageRequests;
import com.example.shop.entity.Customer;
import com.example.shop.exception.BusinessRuleException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.mapper.ShopMapper;
import com.example.shop.repository.CustomerRepository;
import com.example.shop.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Read operations run with {@code readOnly = true}: Hibernate sets FlushMode.MANUAL and skips
 * dirty checking / snapshot copies of loaded entities. Write methods override it with {@code @Transactional}.
 */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "fullName", "email", "createdAt");
    private static final Set<String> ORDER_SORTABLE_FIELDS = Set.of("id", "totalAmount", "status", "createdAt");

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public CustomerPageDto findAll(Integer page, Integer size, String sort, String keyword) {
        Pageable pageable = PageRequests.of(page, size, sort, SORTABLE_FIELDS);
        Page<Customer> result = (keyword == null || keyword.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword.trim(), keyword.trim(), pageable);
        return ShopMapper.toCustomerPage(result);
    }

    public CustomerResponseDto findById(Long id) {
        return ShopMapper.toDto(getCustomer(id));
    }

    public OrderPageDto findOrders(Long customerId, Integer page, Integer size, String sort) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("khách hàng", customerId);
        }
        Pageable pageable = PageRequests.of(page, size, sort, ORDER_SORTABLE_FIELDS);
        return ShopMapper.toOrderPage(orderRepository.findByCustomerId(customerId, pageable));
    }

    @Transactional
    public CustomerResponseDto create(CustomerRequestDto request) {
        ensureEmailAvailable(request.getEmail(), null);
        Customer customer = new Customer();
        applyFull(customer, request);
        return ShopMapper.toDto(customerRepository.saveAndFlush(customer));
    }

    @Transactional
    public CustomerResponseDto replace(Long id, CustomerRequestDto request) {
        Customer customer = getCustomer(id);
        ensureEmailAvailable(request.getEmail(), id);
        applyFull(customer, request);
        return ShopMapper.toDto(customerRepository.saveAndFlush(customer));
    }

    @Transactional
    public CustomerResponseDto update(Long id, CustomerPatchRequestDto request) {
        Customer customer = getCustomer(id);
        if (request.getEmail() != null) {
            ensureEmailAvailable(request.getEmail(), id);
            customer.setEmail(request.getEmail().trim());
        }
        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        return ShopMapper.toDto(customerRepository.saveAndFlush(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = getCustomer(id);
        if (orderRepository.existsByCustomerId(id)) {
            throw new BusinessRuleException("CUSTOMER_HAS_ORDERS",
                    "Không thể xóa khách hàng id = %d vì đã có đơn hàng".formatted(id));
        }
        customerRepository.delete(customer);
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("khách hàng", id));
    }

    private void ensureEmailAvailable(String email, Long currentId) {
        String normalized = email.trim();
        boolean taken = currentId == null
                ? customerRepository.existsByEmailIgnoreCase(normalized)
                : customerRepository.existsByEmailIgnoreCaseAndIdNot(normalized, currentId);
        if (taken) {
            throw BusinessRuleException.onField("EMAIL_ALREADY_EXISTS",
                    "Email đã được sử dụng bởi khách hàng khác",
                    "email", "Email '%s' đã tồn tại".formatted(normalized));
        }
    }

    private static void applyFull(Customer customer, CustomerRequestDto request) {
        customer.setFullName(request.getFullName().trim());
        customer.setEmail(request.getEmail().trim());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
    }
}
