package com.fiap.mecanica.cliente.controller.mapper;

import com.fiap.mecanica.cliente.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.cliente.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.Endereco;
import com.fiap.mecanica.cliente.dto.ClienteDto;
import com.fiap.mecanica.cliente.dto.EnderecoDto;

public class ClienteMapper {

    private ClienteMapper() {
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

    public static ClienteDto toDto(AtualizarClienteRequest request) {
        return ClienteDto.builder()
                .nome(request.nome())
                .documento(apenasDigitos(request.documento()))
                .email(request.email())
                .telefone(apenasDigitos(request.telefone()))
                .endereco(toDto(sanitizarEndereco(request.endereco())))
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

    public static Endereco toEntity(EnderecoDto enderecoDto) {
        if (enderecoDto == null) return null;
        return Endereco.builder()
                .cep(enderecoDto.cep())
                .estado(enderecoDto.estado())
                .cidade(enderecoDto.cidade())
                .bairro(enderecoDto.bairro())
                .rua(enderecoDto.rua())
                .numero(enderecoDto.numero())
                .complemento(enderecoDto.complemento())
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
