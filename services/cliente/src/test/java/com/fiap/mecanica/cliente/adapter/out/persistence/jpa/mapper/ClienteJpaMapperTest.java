package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.EnderecoJpaEmbeddable;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.Endereco;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteJpaMapperTest {

    private static final Long ID = 1L;
    private static final String NOME = "José da Silva";
    private static final String DOCUMENTO = "12345678900";
    private static final String EMAIL = "cliente@email.com";
    private static final String TELEFONE = "31998000000";
    private static final String CEP = "30000000";

    // ===================== toDomain =====================

    @Test
    void deveConverterEntityParaDomainComEnderecoPreenchido() {
        ClienteJpaEntity entity = criarEntity();

        Cliente cliente = ClienteJpaMapper.toDomain(entity);

        assertThat(cliente.getId()).isEqualTo(ID);
        assertThat(cliente.getNome()).isEqualTo(NOME);
        assertThat(cliente.getDocumento()).isEqualTo(DOCUMENTO);
        assertThat(cliente.getEmail()).isEqualTo(EMAIL);
        assertThat(cliente.getTelefone()).isEqualTo(TELEFONE);
        assertThat(cliente.getEndereco().getCep()).isEqualTo(CEP);
        assertThat(cliente.getEndereco().getEstado()).isEqualTo("MG");
        assertThat(cliente.getEndereco().getCidade()).isEqualTo("Belo Horizonte");
        assertThat(cliente.getEndereco().getBairro()).isEqualTo("Centro");
        assertThat(cliente.getEndereco().getRua()).isEqualTo("Rua A");
        assertThat(cliente.getEndereco().getNumero()).isEqualTo("10");
        assertThat(cliente.getEndereco().getComplemento()).isEqualTo("Ap 1");
    }

    @Test
    void deveConverterEntityParaDomainComEnderecoNulo() {
        ClienteJpaEntity entity = criarEntity();
        entity.setEndereco(null);

        Cliente cliente = ClienteJpaMapper.toDomain(entity);

        assertThat(cliente.getEndereco()).isNull();
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(ClienteJpaMapper.toDomain(null)).isNull();
    }

    // ===================== toJpaEntity =====================

    @Test
    void deveConverterDomainParaEntityComEnderecoPreenchido() {
        Cliente cliente = criarCliente();

        ClienteJpaEntity entity = ClienteJpaMapper.toJpaEntity(cliente);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getNome()).isEqualTo(NOME);
        assertThat(entity.getDocumento()).isEqualTo(DOCUMENTO);
        assertThat(entity.getEmail()).isEqualTo(EMAIL);
        assertThat(entity.getTelefone()).isEqualTo(TELEFONE);
        assertThat(entity.getEndereco().getCep()).isEqualTo(CEP);
        assertThat(entity.getEndereco().getEstado()).isEqualTo("MG");
        assertThat(entity.getEndereco().getCidade()).isEqualTo("Belo Horizonte");
        assertThat(entity.getEndereco().getBairro()).isEqualTo("Centro");
        assertThat(entity.getEndereco().getRua()).isEqualTo("Rua A");
        assertThat(entity.getEndereco().getNumero()).isEqualTo("10");
        assertThat(entity.getEndereco().getComplemento()).isEqualTo("Ap 1");
    }

    @Test
    void deveConverterDomainParaEntityComEnderecoNulo() {
        Cliente cliente = criarCliente();
        cliente.setEndereco(null);

        ClienteJpaEntity entity = ClienteJpaMapper.toJpaEntity(cliente);

        assertThat(entity.getEndereco()).isNull();
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(ClienteJpaMapper.toJpaEntity(null)).isNull();
    }

    private ClienteJpaEntity criarEntity() {
        return ClienteJpaEntity.builder()
                .id(ID)
                .nome(NOME)
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(EnderecoJpaEmbeddable.builder()
                        .cep(CEP).estado("MG").cidade("Belo Horizonte")
                        .bairro("Centro").rua("Rua A").numero("10").complemento("Ap 1")
                        .build())
                .build();
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID)
                .nome(NOME)
                .documento(DOCUMENTO)
                .email(EMAIL)
                .telefone(TELEFONE)
                .endereco(Endereco.builder()
                        .cep(CEP).estado("MG").cidade("Belo Horizonte")
                        .bairro("Centro").rua("Rua A").numero("10").complemento("Ap 1")
                        .build())
                .build();
    }
}
