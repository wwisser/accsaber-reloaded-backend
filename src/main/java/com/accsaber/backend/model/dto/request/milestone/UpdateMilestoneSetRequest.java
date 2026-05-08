package com.accsaber.backend.model.dto.request.milestone;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class UpdateMilestoneSetRequest {

    private String title;

    private String description;

    private BigDecimal setBonusXp;

    private UUID awardsItemId;
}
