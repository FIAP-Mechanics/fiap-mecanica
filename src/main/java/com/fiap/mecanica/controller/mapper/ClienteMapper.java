package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.dto.ClienteDto;

public class ClienteMapper {

    private ClienteMapper(){}

    public static Cliente toEntity(CadastrarClienteRequest request){
        return Cliente.builder()
                .nome(request.nome())
                .documento(apenasDigitos(request.documento()))
                .email(request.email())
                .telefone(apenasDigitos(request.telefone()))
                .endereco(sanitizarEndereco(request.endereco()))
                .build();
    }

    public static ClienteDto toDto(AtualizarClienteRequest request){
        return ClienteDto.builder()
                .nome(request.nome())
                .documento(apenasDigitos(request.documento()))
                .email(request.email())
                .telefone(apenasDigitos(request.telefone()))
                .endereco(sanitizarEndereco(request.endereco()))
                .build();
    }

    public static ClienteDto toDto(Cliente cliente){
        return ClienteDto.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .build();
    }

    static String apenasDigitos(String valor) {
        if (valor == null) return null;
        return valor.replaceAll("\\D", "");
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
