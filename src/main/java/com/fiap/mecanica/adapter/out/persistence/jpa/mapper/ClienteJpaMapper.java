package com.fiap.mecanica.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.adapter.out.persistence.jpa.entity.EnderecoJpaEmbeddable;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;

public final class ClienteJpaMapper {

    private ClienteJpaMapper() {
    }

    public static ClienteJpaEntity toJpaEntity(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteJpaEntity.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(toJpaEmbeddable(cliente.getEndereco()))
                .build();
    }

    public static Cliente toDomain(ClienteJpaEntity entity) {
        if (entity == null) return null;
        return Cliente.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .documento(entity.getDocumento())
                .email(entity.getEmail())
                .telefone(entity.getTelefone())
                .endereco(toDomain(entity.getEndereco()))
                .build();
    }

    private static EnderecoJpaEmbeddable toJpaEmbeddable(Endereco endereco) {
        if (endereco == null) return null;
        return EnderecoJpaEmbeddable.builder()
                .cep(endereco.getCep())
                .estado(endereco.getEstado())
                .cidade(endereco.getCidade())
                .bairro(endereco.getBairro())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .build();
    }

    private static Endereco toDomain(EnderecoJpaEmbeddable endereco) {
        if (endereco == null) return null;
        return Endereco.builder()
                .cep(endereco.getCep())
                .estado(endereco.getEstado())
                .cidade(endereco.getCidade())
                .bairro(endereco.getBairro())
                .rua(endereco.getRua())
                .numero(endereco.getNumero())
                .complemento(endereco.getComplemento())
                .build();
    }
}
