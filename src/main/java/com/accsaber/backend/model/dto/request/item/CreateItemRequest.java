package com.accsaber.backend.model.dto.request.item;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateItemRequest {

    @NotNull
    private UUID typeId;

    @NotBlank
    private String name;

    private String description;
    private String iconUrl;
    private Map<String, Object> value;

    private boolean tradeable = false;
    private boolean visible = true;
}
