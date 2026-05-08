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
    private ModifierRef modifier;
    private Long serialNumber;
    private String source;
    private String sourceId;
    private UUID awardedByStaffId;
    private String reason;
    private Instant awardedAt;

    @Getter
    @Builder
    public static class ModifierRef {
        private UUID id;
        private String key;
        private String name;
    }
}
