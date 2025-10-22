package com.brokerage.core.api.asset.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal size;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal usableSize;
}

