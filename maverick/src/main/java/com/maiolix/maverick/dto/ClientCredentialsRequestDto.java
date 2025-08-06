package com.maiolix.maverick.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO per la richiesta di token client (client_credentials flow)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCredentialsRequestDto {

    private String clientId;
    private String clientSecret;
    private String grantType;
}
