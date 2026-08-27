package com.fiap.mecanica.cliente.adapter.in.web.presenter;

import com.fiap.mecanica.cliente.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.cliente.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.cliente.adapter.in.web.response.EnderecoDto;
import com.fiap.mecanica.cliente.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.cliente.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import com.fiap.mecanica.cliente.domain.Endereco;

public class ClientePresenter {

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
        return AtualizarClienteCommand.builder()
                .nome(request.nome())
                .documento(apenasDigitos(request.documento()))
                .email(request.email())
                .telefone(apenasDigitos(request.telefone()))
                .endereco(sanitizarEndereco(request.endereco()))
                .build();
    }

    public static ClienteDto toDto(Cliente cliente) {
        return ClienteDto.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(toDto(cliente.getEndereco()))
                .build();
    }

    public static VeiculoDto toDto(ClienteVeiculo clienteVeiculo) {
        return VeiculoDto.builder()
                .id(clienteVeiculo.getVeiculoId())
                .marca(clienteVeiculo.getMarca())
                .modelo(clienteVeiculo.getModelo())
                .placa(clienteVeiculo.getPlaca())
                .ano(clienteVeiculo.getAno())
                .build();
    }

    private static EnderecoDto toDto(Endereco endereco) {
        if (endereco == null) return null;
        return EnderecoDto.builder()
                .cep(endereco.getCep())
                .estado(endereco.getEstado())
                .cidade(endereco.getCidade())
                .bairro(endereco.getBairro())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .build();
    }

    private static String apenasDigitos(String valor) {
        return valor != null ? valor.replaceAll("\\D", "") : "";
    }

    private static Endereco sanitizarEndereco(Endereco endereco) {
        if (endereco == null) return null;
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
