package com.brokerage.core.controller.mapper;

import com.brokerage.core.controller.dto.AssetDto;
import com.brokerage.core.model.Asset;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetDto toDto(Asset asset);
    List<AssetDto> toDtoList(List<Asset> assets);
}
