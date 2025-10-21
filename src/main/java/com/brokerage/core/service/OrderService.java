package com.brokerage.core.service;

import com.brokerage.core.constants.ErrorKeys;
import com.brokerage.core.controller.dto.CreateOrderRequest;
import com.brokerage.core.controller.dto.ListOrdersRequest;
import com.brokerage.core.controller.dto.OrderResponse;
import com.brokerage.core.controller.mapper.OrderMapper;
import com.brokerage.core.enumaration.OrderSide;
import com.brokerage.core.enumaration.OrderStatus;
import com.brokerage.core.exception.BusinessException;
import com.brokerage.core.exception.ResourceNotFoundException;
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
    private final OrderMapper orderMapper;

    private static final String TRY_ASSET = "TRY";

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest dto) {
        UUID customerId = dto.customerId();
        String assetName = dto.assetName();
        BigDecimal size = dto.size();
        BigDecimal price = dto.price();
        OrderSide side = dto.orderSide();

        if (side == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY_ASSET)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.TRY_NOT_FOUND));
            BigDecimal totalCost = size.multiply(price);
            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
                throw new BusinessException(ErrorKeys.INSUFFICIENT_BALANCE);
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
            assetRepository.save(tryAsset);
        } else {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.ASSET_NOT_FOUND));
            if (asset.getUsableSize().compareTo(size) < 0) {
                throw new BusinessException(ErrorKeys.INSUFFICIENT_BALANCE);
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

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorKeys.ORDER_NOT_PENDING);
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY_ASSET)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.TRY_NOT_FOUND));
            BigDecimal refund = order.getSize().multiply(order.getPrice());
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(refund));
            assetRepository.save(tryAsset);
        } else {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.ASSET_NOT_FOUND));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    @Transactional
    public OrderResponse matchOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorKeys.ORDER_NOT_PENDING);
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseGet(() -> Asset.builder()
                            .customerId(order.getCustomerId())
                            .assetName(order.getAssetName())
                            .size(BigDecimal.ZERO)
                            .usableSize(BigDecimal.ZERO)
                            .build());

            asset.setSize(asset.getSize().add(order.getSize()));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        } else {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY_ASSET)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.TRY_NOT_FOUND));

            BigDecimal totalIncome = order.getSize().multiply(order.getPrice());
            tryAsset.setSize(tryAsset.getSize().add(totalIncome));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalIncome));
            assetRepository.save(tryAsset);
        }

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);

        return orderMapper.toDto(order);
    }

    public List<OrderResponse> listOrders(ListOrdersRequest request) {
        var orders = orderRepository.findByCustomerIdAndCreateDateBetween(request.customerId(), request.start(), request.end());
        return orderMapper.toDtoList(orders);
    }

    public List<OrderResponse> listPendingOrders() {
        return orderMapper.toDtoList(orderRepository.findByStatus(OrderStatus.PENDING));
    }
}