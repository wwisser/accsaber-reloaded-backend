package com.accsaber.backend.model.dto.response.item;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemResponse {

    private UUID id;
    private UUID typeId;
    private String typeKey;
    private String name;
    private String description;
    private String iconUrl;
    private Object value;
    private boolean tradeable;
    private boolean visible;
    private boolean active;
    private Instant createdAt;
}
