package com.brokerage.core.controller;


import com.brokerage.core.constants.SuccessKeys;
import com.brokerage.core.controller.dto.CreateOrderRequest;
import com.brokerage.core.controller.dto.ListOrdersRequest;
import com.brokerage.core.response.BaseResponse;
import com.brokerage.core.service.OrderService;
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

    public OrderController(MessageSource messageSource, OrderService orderService) {
        super(messageSource);
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return created(SuccessKeys.ORDER_CREATED, orderService.createOrder(request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrder(orderId);
        return noContent(SuccessKeys.ORDER_CANCELED);
    }

    @PostMapping("/list")
    public ResponseEntity<?> listOrders(@Valid @RequestBody ListOrdersRequest request) {
        return ok(SuccessKeys.GENERIC_SUCCESS, orderService.listOrders(request));
    }

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

