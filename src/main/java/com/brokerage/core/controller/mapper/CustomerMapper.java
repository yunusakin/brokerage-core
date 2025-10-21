package com.brokerage.core.controller.mapper;


import com.brokerage.core.controller.dto.CustomerDto;
import com.brokerage.core.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
}

