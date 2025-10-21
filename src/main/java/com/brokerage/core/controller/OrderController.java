package com.brokerage.core.controller;



import com.brokerage.core.controller.dto.OrderRequestDto;
import com.brokerage.core.controller.dto.OrderResponseDto;
import com.brokerage.core.controller.mapper.OrderMapper;
import com.brokerage.core.model.Order;
import com.brokerage.core.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto dto) {
        // Service still enforces validation + business logic
        Order order = orderService.createOrder(
                dto.customerId(),
                dto.assetName(),
                dto.side(),
                dto.size(),
                dto.price()
        );
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam UUID customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        var orders = orderService.listOrders(customerId, start, end);
        return ResponseEntity.ok(orderMapper.toDtoList(orders));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<OrderResponseDto>> listPendingOrders() {
        var pending = orderService.getPendingOrders();
        return ResponseEntity.ok(orderMapper.toDtoList(pending));
    }
}

