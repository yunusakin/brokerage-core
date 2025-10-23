package com.brokerage.core.api.order.service;

import com.brokerage.core.api.asset.model.Asset;
import com.brokerage.core.api.order.model.Order;
import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.api.order.dto.CreateOrderRequest;
import com.brokerage.core.api.order.dto.ListOrdersRequest;
import com.brokerage.core.api.order.dto.OrderResponse;
import com.brokerage.core.api.order.mapper.OrderMapper;
import com.brokerage.core.base.enumaration.OrderSide;
import com.brokerage.core.base.enumaration.OrderStatus;
import com.brokerage.core.base.exception.BusinessException;
import com.brokerage.core.base.exception.ResourceNotFoundException;
import com.brokerage.core.api.asset.repository.AssetRepository;
import com.brokerage.core.api.order.repository.OrderRepository;
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
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY_ASSET)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.TRY_NOT_FOUND));

            BigDecimal totalCost = order.getSize().multiply(order.getPrice());
            if (tryAsset.getSize().compareTo(totalCost) < 0) {
                throw new BusinessException(ErrorKeys.INSUFFICIENT_BALANCE);
            }

            tryAsset.setSize(tryAsset.getSize().subtract(totalCost));
            assetRepository.save(tryAsset);

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
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorKeys.ASSET_NOT_FOUND));

            if (asset.getSize().compareTo(order.getSize()) < 0) {
                throw new BusinessException(ErrorKeys.INSUFFICIENT_BALANCE);
            }

            asset.setSize(asset.getSize().subtract(order.getSize()));
            assetRepository.save(asset);

            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), TRY_ASSET)
                    .orElseGet(() -> Asset.builder()
                            .customerId(order.getCustomerId())
                            .assetName(TRY_ASSET)
                            .size(BigDecimal.ZERO)
                            .usableSize(BigDecimal.ZERO)
                            .build());

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