package com.accsaber.backend.model.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "ticket")
public class IngameAuthRequest {

    @NotBlank
    private String provider;

    @NotBlank
    private String ticket;
}
