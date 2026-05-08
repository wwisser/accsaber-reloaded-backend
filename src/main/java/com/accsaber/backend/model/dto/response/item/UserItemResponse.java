package com.accsaber.backend.model.dto.response.item;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserItemResponse {

    private UUID linkId;
    private ItemResponse item;
    private String source;
    private String sourceId;
    private UUID awardedByStaffId;
    private String reason;
    private Instant awardedAt;
}
