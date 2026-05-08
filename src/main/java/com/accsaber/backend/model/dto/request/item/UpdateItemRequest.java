package com.accsaber.backend.model.dto.request.item;

import java.util.Map;

import lombok.Data;

@Data
public class UpdateItemRequest {

    private String name;
    private String description;
    private String iconUrl;
    private Map<String, Object> value;
    private Boolean tradeable;
    private Boolean visible;
}
