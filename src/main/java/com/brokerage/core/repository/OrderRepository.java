package com.brokerage.core.repository;

import com.brokerage.core.enumaration.OrderStatus;
import com.brokerage.core.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByCustomerIdAndCreateDateBetween(UUID customerId, LocalDateTime start, LocalDateTime end);
    List<Order> findByStatus(OrderStatus status);
    @Query("SELECT o FROM Order o WHERE o.assetName = :assetName AND o.status = 'PENDING'")
    List<Order> findPendingByAssetName(String assetName);
}