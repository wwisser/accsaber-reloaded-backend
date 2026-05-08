package com.accsaber.backend.model.dto.request.item;

import java.util.Map;

import lombok.Data;

@Data
public class UpdateItemTypeRequest {

    private String name;
    private String description;
    private Map<String, Object> valueSchema;
}
