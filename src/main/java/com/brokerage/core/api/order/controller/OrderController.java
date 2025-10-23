package com.brokerage.core.api.order.controller;


import com.brokerage.core.base.constants.SuccessKeys;
import com.brokerage.core.api.order.dto.CreateOrderRequest;
import com.brokerage.core.api.order.dto.ListOrdersRequest;
import com.brokerage.core.base.response.BaseResponse;
import com.brokerage.core.api.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController extends BaseResponse {

    private final OrderService orderService;
    private final com.brokerage.core.base.security.CurrentUserService currentUser;
    private final com.brokerage.core.api.order.repository.OrderRepository orderRepository;

    public OrderController(MessageSource messageSource, OrderService orderService,
                           com.brokerage.core.base.security.CurrentUserService currentUser,
                           com.brokerage.core.api.order.repository.OrderRepository orderRepository) {
        super(messageSource);
        this.orderService = orderService;
        this.currentUser = currentUser;
        this.orderRepository = orderRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        if (!currentUser.isAdmin()) {
            UUID self = currentUser.currentCustomerId();
            // override any supplied customerId with the authenticated customer's id
            request = new CreateOrderRequest(self, request.assetName(), request.orderSide(), request.size(), request.price());
        }
        return created(SuccessKeys.ORDER_CREATED, orderService.createOrder(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId) {
        if (!currentUser.isAdmin()) {
            UUID self = currentUser.currentCustomerId();
            var order = orderRepository.findById(orderId).orElseThrow();
            if (self == null || !order.getCustomerId().equals(self)) {
                throw new org.springframework.security.access.AccessDeniedException(com.brokerage.core.base.constants.ErrorKeys.ACCESS_DENIED);
            }
        }
        orderService.cancelOrder(orderId);
        return noContent(SuccessKeys.ORDER_CANCELED);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    @PostMapping("/list")
    public ResponseEntity<?> listOrders(@Valid @RequestBody ListOrdersRequest request) {
        if (!currentUser.isAdmin()) {
            UUID self = currentUser.currentCustomerId();
            request = new ListOrdersRequest(self, request.start(), request.end());
        }
        return ok(SuccessKeys.GENERIC_SUCCESS, orderService.listOrders(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<?> listPendingOrders() {
        return ok(SuccessKeys.GENERIC_SUCCESS, orderService.listPendingOrders());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/match")
    public ResponseEntity<?> matchOrder(@PathVariable UUID orderId) {
        return ok(SuccessKeys.ORDER_MATCHED, orderService.matchOrder(orderId));
    }
}

