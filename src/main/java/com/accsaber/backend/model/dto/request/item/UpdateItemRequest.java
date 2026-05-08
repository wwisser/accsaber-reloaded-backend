package com.accsaber.backend.model.dto.request.item;

import java.util.Map;

import com.accsaber.backend.model.entity.item.ItemRarity;

import lombok.Data;

@Data
public class UpdateItemRequest {

    private String name;
    private String description;
    private String iconUrl;
    private Map<String, Object> value;
    private ItemRarity rarity;
    private Boolean tradeable;
    private Boolean visible;
}
