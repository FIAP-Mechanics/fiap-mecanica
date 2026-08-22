package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;

public final class ClientePresenter {

    private ClientePresenter() {
    }

    public static Cliente toEntity(CadastrarClienteRequest request) {
        return Cliente.builder()
                .nome(request.nome())
                .documento(apenasDigitos(request.documento()))
                .email(request.email())
                .telefone(apenasDigitos(request.telefone()))
                .endereco(sanitizarEndereco(request.endereco()))
                .build();
    }

    public static AtualizarClienteCommand toCommand(AtualizarClienteRequest request) {
        return new AtualizarClienteCommand(
                request.nome(),
                apenasDigitos(request.documento()),
                request.email(),
                apenasDigitos(request.telefone()),
                sanitizarEndereco(request.endereco()));
    }

    public static ClienteDto toDto(Cliente cliente) {
        return ClienteDto.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .build();
    }

    private static String apenasDigitos(String valor) {
        return valor.replaceAll("\\D", "");
    }

    private static Endereco sanitizarEndereco(Endereco endereco) {
        return Endereco.builder()
                .cep(apenasDigitos(endereco.getCep()))
                .estado(endereco.getEstado())
                .cidade(endereco.getCidade())
                .bairro(endereco.getBairro())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .build();
    }
}
