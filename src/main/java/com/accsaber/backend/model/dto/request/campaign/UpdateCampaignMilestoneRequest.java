package com.accsaber.backend.model.dto.request.campaign;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateCampaignMilestoneRequest {

    private String title;

    private String description;

    private String avatarUrl;

    private BigDecimal xp;

    private UUID awardsItemId;
}
