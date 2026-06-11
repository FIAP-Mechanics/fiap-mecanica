package com.fiap.mecanica.dto;

import com.fiap.mecanica.domain.Endereco;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Dados do cliente")
public record ClienteDto(
        @Schema(description = "Nome do cliente", example = "Jose da Silva") String nome,
        @Schema(description = "Identificador único do clinete", example = "1") Long id,
        @Schema(description = "CPF/CNPJ único do clinete", example = "123.456.789-99") String documento,
        @Schema(description = "E-mail único do clinete", example = "cliente@gmail.com") String email,
        @Schema(description = "Telefone do clinete", example = "31998495612") String telefone,
        @Schema(description = "Endereço/residencia do clinete") Endereco endereco
        ){}
