package com.accsaber.backend.model.dto.response.milestone;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelThresholdResponse {

    private int level;
    private String title;
    private UUID awardsItemId;
    private Instant createdAt;
    private Instant updatedAt;
}
