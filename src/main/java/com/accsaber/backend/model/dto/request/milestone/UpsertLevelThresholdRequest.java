package com.accsaber.backend.model.dto.request.milestone;

import java.util.UUID;

import lombok.Data;

@Data
public class UpsertLevelThresholdRequest {

    private String title;

    private UUID awardsItemId;
}
