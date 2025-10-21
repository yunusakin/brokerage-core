package com.brokerage.core.service;

import com.brokerage.core.enumaration.OrderSide;
import com.brokerage.core.enumaration.OrderStatus;
import com.brokerage.core.model.*;
import com.brokerage.core.repository.AssetRepository;
import com.brokerage.core.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    private static final String TRY_ASSET = "TRY";

    @Transactional
    public Order createOrder(UUID customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        if (side == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY_ASSET).orElseThrow(() -> new IllegalStateException("TRY balance not found"));
            BigDecimal totalCost = size.multiply(price);
            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
                throw new IllegalStateException("Insufficient TRY balance");
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
            assetRepository.save(tryAsset);

        } else {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new IllegalStateException("Asset not found: " + assetName));
            if (asset.getUsableSize().compareTo(size) < 0) {
                throw new IllegalStateException("Insufficient asset balance");
            }
            asset.setUsableSize(asset.getUsableSize().subtract(size));
            assetRepository.save(asset);
        }

        Order order = Order.builder()
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(side)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be canceled");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY_ASSET)
                    .orElseThrow(() -> new IllegalStateException("TRY balance not found"));
            BigDecimal refund = order.getSize().multiply(order.getPrice());
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(refund));
            assetRepository.save(tryAsset);
        } else {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset not found"));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public List<Order> listOrders(UUID customerId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, start, end);
    }
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }
}

