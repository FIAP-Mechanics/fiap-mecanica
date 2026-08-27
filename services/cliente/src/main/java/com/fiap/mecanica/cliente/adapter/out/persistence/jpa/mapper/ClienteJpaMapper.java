package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.EnderecoJpaEmbeddable;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.Endereco;

public class ClienteJpaMapper {

    private ClienteJpaMapper() {
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

    public static ClienteJpaEntity toJpaEntity(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteJpaEntity.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .documento(cliente.getDocumento())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .endereco(toJpaEntity(cliente.getEndereco()))
                .build();
    }

    private static Endereco toDomain(EnderecoJpaEmbeddable embeddable) {
        if (embeddable == null) return null;
        return Endereco.builder()
                .cep(embeddable.getCep())
                .estado(embeddable.getEstado())
                .cidade(embeddable.getCidade())
                .bairro(embeddable.getBairro())
                .rua(embeddable.getRua())
                .numero(embeddable.getNumero())
                .complemento(embeddable.getComplemento())
                .build();
    }

    private static EnderecoJpaEmbeddable toJpaEntity(Endereco endereco) {
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
}
