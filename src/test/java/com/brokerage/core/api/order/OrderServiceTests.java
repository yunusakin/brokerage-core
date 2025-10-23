package com.brokerage.core.api.order;

import com.brokerage.core.api.asset.model.Asset;
import com.brokerage.core.api.asset.repository.AssetRepository;
import com.brokerage.core.api.order.dto.CreateOrderRequest;
import com.brokerage.core.api.order.dto.ListOrdersRequest;
import com.brokerage.core.api.order.model.Order;
import com.brokerage.core.api.order.repository.OrderRepository;
import com.brokerage.core.api.order.service.OrderService;
import com.brokerage.core.base.constants.ErrorKeys;
import com.brokerage.core.base.enumaration.OrderSide;
import com.brokerage.core.base.enumaration.OrderStatus;
import com.brokerage.core.base.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceTests {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AssetRepository assetRepository;
    @Autowired
    private OrderRepository orderRepository;

    private static final String TRY = "TRY";

    private void seedAsset(UUID customerId, String assetName, BigDecimal size, BigDecimal usable) {
        Asset a = Asset.builder()
                .customerId(customerId)
                .assetName(assetName)
                .size(size)
                .usableSize(usable)
                .build();
        assetRepository.save(a);
    }

    private CreateOrderRequest req(UUID customerId, String asset, OrderSide side, BigDecimal size, BigDecimal price) {
        return new CreateOrderRequest(customerId, asset, side, size, price);
    }

    @Test
    @Transactional
    void createBuy_reservesTryUsable_andCreatesPending() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(20000), bd(20000));

        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(10), bd(1000)));

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow();
        assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(bd(10000));
    }

    @Test
    @Transactional
    void createBuy_insufficientTry_throwsBusinessException() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(5000), bd(5000));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(10), bd(1000))));
        assertThat(ex.getMessage()).isEqualTo(ErrorKeys.INSUFFICIENT_BALANCE);
    }

    @Test
    @Transactional
    void createSell_reservesAssetUsable() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, "AAPL", bd(50), bd(50));

        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.SELL, bd(10), bd(100)));
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);

        Asset aapl = assetRepository.findByCustomerIdAndAssetName(customerId, "AAPL").orElseThrow();
        assertThat(aapl.getUsableSize()).isEqualByComparingTo(bd(40));
    }

    @Test
    @Transactional
    void cancelBuy_releasesTryUsable_andSetsCanceled() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(20000), bd(20000));
        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(10), bd(1000)));

        orderService.cancelOrder(order.id());

        Order persisted = orderRepository.findById(order.id()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CANCELED);

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow();
        assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(bd(20000));
    }

    @Test
    @Transactional
    void cancelTwice_throwsOrderNotPending() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(10000), bd(10000));
        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(5), bd(1000)));

        orderService.cancelOrder(order.id());
        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.cancelOrder(order.id()));
        assertThat(ex.getMessage()).isEqualTo(ErrorKeys.ORDER_NOT_PENDING);
    }

    @Test
    @Transactional
    void matchBuy_updatesTry_andAsset_andSetsMatched() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(30000), bd(30000));
        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(10), bd(1000)));

        Asset tryAssetBefore = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow();
        BigDecimal trySizeBefore = tryAssetBefore.getSize();

        var matched = orderService.matchOrder(order.id());
        assertThat(matched.status()).isEqualTo(OrderStatus.MATCHED);

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow();
        assertThat(tryAsset.getSize()).isEqualByComparingTo(trySizeBefore.subtract(bd(10000)));

        Asset bought = assetRepository.findByCustomerIdAndAssetName(customerId, "AAPL").orElseThrow();
        assertThat(bought.getSize()).isEqualByComparingTo(bd(10));
        assertThat(bought.getUsableSize()).isEqualByComparingTo(bd(10));
    }

    @Test
    @Transactional
    void matchSell_updatesAsset_andTry() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, "AAPL", bd(50), bd(50));
        seedAsset(customerId, TRY, bd(5000), bd(5000));

        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.SELL, bd(10), bd(100)));

        var matched = orderService.matchOrder(order.id());
        assertThat(matched.status()).isEqualTo(OrderStatus.MATCHED);

        Asset aapl = assetRepository.findByCustomerIdAndAssetName(customerId, "AAPL").orElseThrow();
        assertThat(aapl.getSize()).isEqualByComparingTo(bd(40));

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow();
        assertThat(tryAsset.getSize()).isEqualByComparingTo(bd(6000));
        assertThat(tryAsset.getUsableSize()).isEqualByComparingTo(bd(6000));
    }

    @Test
    @Transactional
    void listOrders_byDateRange_returnsExpected() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(100000), bd(100000));
        orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(1), bd(10)));
        orderService.createOrder(req(customerId, "AAPL", OrderSide.BUY, bd(2), bd(10)));

        LocalDateTime now = LocalDateTime.now();
        var list = orderService.listOrders(new ListOrdersRequest(customerId, now.minusDays(1), now.plusDays(1)));
        assertThat(list).hasSize(2);
    }

    @Test
    @Transactional
    void createOrder_withTRY_asset_shouldFail() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, TRY, bd(1000), bd(1000));
        BusinessException ex = assertThrows(BusinessException.class, () ->
                orderService.createOrder(req(customerId, "TRY", OrderSide.BUY, bd(1), bd(1))));
        assertThat(ex.getMessage()).isEqualTo(com.brokerage.core.base.constants.ErrorKeys.TRY_NOT_TRADABLE);
    }

    @Test
    @Transactional
    void cancelSell_releasesAssetUsable_andSetsCanceled() {
        UUID customerId = UUID.randomUUID();
        seedAsset(customerId, "AAPL", bd(50), bd(50));

        var order = orderService.createOrder(req(customerId, "AAPL", OrderSide.SELL, bd(10), bd(100)));

        orderService.cancelOrder(order.id());

        Order persisted = orderRepository.findById(order.id()).orElseThrow();
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.CANCELED);

        Asset aapl = assetRepository.findByCustomerIdAndAssetName(customerId, "AAPL").orElseThrow();
        assertThat(aapl.getUsableSize()).isEqualByComparingTo(bd(50));
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(4, java.math.RoundingMode.HALF_UP);
    }
}
