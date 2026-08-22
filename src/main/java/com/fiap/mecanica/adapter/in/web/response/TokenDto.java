package com.fiap.mecanica.adapter.in.web.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token OAuth2 Bearer JWT")
public record TokenDto(
        @JsonProperty("access_token") @Schema(description = "Token JWT de acesso") String accessToken,
        @JsonProperty("token_type") @Schema(description = "Tipo do token", example = "Bearer") String tokenType,
        @JsonProperty("expires_in") @Schema(description = "Tempo de expiracao em segundos", example = "3600") Long expiresIn) {
}
