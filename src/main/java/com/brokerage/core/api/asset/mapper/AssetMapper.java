package com.brokerage.core.api.asset.mapper;

import com.brokerage.core.api.asset.dto.AssetDto;
import com.brokerage.core.api.asset.model.Asset;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetDto toDto(Asset asset);
    List<AssetDto> toDtoList(List<Asset> assets);
}
