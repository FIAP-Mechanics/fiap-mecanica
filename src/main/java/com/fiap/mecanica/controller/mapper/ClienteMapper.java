package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.dto.ClienteDto;

public class ClienteMapper {

    private ClienteMapper(){}

    public static Cliente toEntity(CadastrarClienteRequest request){
        return Cliente.builder()
                .documento(request.documento())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(request.endereco())
                .build();
    }

    public static ClienteDto toDto(AtualizarClienteRequest request){
        return ClienteDto.builder()
                .documento(request.documento())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(request.endereco())
                .build();
    }

    public static ClienteDto toDto(Cliente cliente){
        return ClienteDto.builder()
                .id(cliente.getId())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(cliente.getEndereco())
                .build();
    }
}
