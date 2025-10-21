package com.brokerage.core.controller.mapper;

import com.brokerage.core.controller.dto.OrderRequestDto;
import com.brokerage.core.controller.dto.OrderResponseDto;
import com.brokerage.core.model.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createDate", expression = "java(java.time.LocalDateTime.now())")
    Order toEntity(OrderRequestDto dto);

    OrderResponseDto toDto(Order order);

    List<OrderResponseDto> toDtoList(List<Order> orders);
}
