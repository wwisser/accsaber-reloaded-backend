package com.accsaber.backend.model.dto.response.item;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TradeResponse {

    private UUID id;
    private Long fromUserId;
    private Long toUserId;
    private UUID userItemLinkId;
    private ItemResponse item;
    private UserItemResponse.ModifierRef modifier;
    private Long serialNumber;
    private String status;
    private String message;
    private Instant createdAt;
    private Instant resolvedAt;
}
