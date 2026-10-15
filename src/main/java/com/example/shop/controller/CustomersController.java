package com.example.shop.controller;

import com.example.shop.api.CustomersApi;
import com.example.shop.api.model.CustomerPageDto;
import com.example.shop.api.model.CustomerPatchRequestDto;
import com.example.shop.api.model.CustomerRequestDto;
import com.example.shop.api.model.CustomerResponseDto;
import com.example.shop.api.model.OrderPageDto;
import com.example.shop.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.BASE)
public class CustomersController implements CustomersApi {

    private final CustomerService customerService;

    public CustomersController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public ResponseEntity<CustomerPageDto> listCustomers(Integer page, Integer size, String sort, String keyword) {
        return ResponseEntity.ok(customerService.findAll(page, size, sort, keyword));
    }

    @Override
    public ResponseEntity<CustomerResponseDto> createCustomer(CustomerRequestDto customerRequestDto) {
        CustomerResponseDto created = customerService.create(customerRequestDto);
        return ResponseEntity.created(ApiPaths.locationOf(created.getId())).body(created);
    }

    @Override
    public ResponseEntity<CustomerResponseDto> getCustomerById(Long customerId) {
        return ResponseEntity.ok(customerService.findById(customerId));
    }

    @Override
    public ResponseEntity<CustomerResponseDto> replaceCustomer(Long customerId, CustomerRequestDto customerRequestDto) {
        return ResponseEntity.ok(customerService.replace(customerId, customerRequestDto));
    }

    @Override
    public ResponseEntity<CustomerResponseDto> updateCustomer(Long customerId,
                                                              CustomerPatchRequestDto customerPatchRequestDto) {
        return ResponseEntity.ok(customerService.update(customerId, customerPatchRequestDto));
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(Long customerId) {
        customerService.delete(customerId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OrderPageDto> listCustomerOrders(Long customerId, Integer page, Integer size, String sort) {
        return ResponseEntity.ok(customerService.findOrders(customerId, page, size, sort));
    }
}
