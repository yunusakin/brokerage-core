package com.brokerage.core.api.customer.mapper;


import com.brokerage.core.api.customer.dto.CustomerDto;
import com.brokerage.core.api.customer.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDto toDto(Customer customer);
    Customer toEntity(CustomerDto customerDto);
}

