package com.fiap.mecanica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {
	private Long id;
	private String marca;
	private String modelo;
    private String placa;
	private Integer ano;
	@Builder.Default
	private boolean ativo = true;
}
