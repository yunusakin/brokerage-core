package com.brokerage.core.api.order.model;

import com.brokerage.core.base.enumaration.OrderSide;
import com.brokerage.core.base.enumaration.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide orderSide;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal size;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime createDate;
}

