package com.accsaber.backend.model.dto.response.campaign;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CampaignMilestoneResponse {

    private UUID id;
    private UUID campaignId;
    private String title;
    private String description;
    private String avatarUrl;
    private BigDecimal xp;
    private UUID awardsItemId;
    private boolean active;
    private Instant createdAt;
}
