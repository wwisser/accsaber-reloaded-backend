package com.accsaber.backend.model.dto.request.item;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateItemTypeRequest {

    private UUID parentTypeId;

    @NotBlank
    private String key;

    @NotBlank
    private String name;

    private String description;

    private Map<String, Object> valueSchema;
}
