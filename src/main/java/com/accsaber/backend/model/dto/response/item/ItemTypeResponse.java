package com.accsaber.backend.model.dto.response.item;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemTypeResponse {

    private UUID id;
    private UUID parentTypeId;
    private String key;
    private String name;
    private String description;
    private Object valueSchema;
    private boolean active;
    private Instant createdAt;
}
