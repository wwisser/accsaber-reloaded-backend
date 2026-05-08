package com.accsaber.backend.model.dto.request.item;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTradeRequest {

    @NotNull
    private Long toUserId;

    @NotNull
    private UUID userItemLinkId;

    private String message;
}
