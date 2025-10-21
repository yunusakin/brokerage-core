package com.brokerage.core.controller.mapper;

import com.brokerage.core.controller.dto.CreateOrderRequest;
import com.brokerage.core.controller.dto.OrderResponse;
import com.brokerage.core.model.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createDate", expression = "java(java.time.LocalDateTime.now())")
    Order toEntity(CreateOrderRequest dto);

    OrderResponse toDto(Order order);

    List<OrderResponse> toDtoList(List<Order> orders);
}
