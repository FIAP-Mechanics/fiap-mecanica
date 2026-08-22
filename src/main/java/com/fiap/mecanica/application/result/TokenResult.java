package com.fiap.mecanica.application.result;

public record TokenResult(String accessToken, String tokenType, Long expiresIn) {
}
