package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.mapper;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteVeiculoJpaMapperTest {

    private static final Long ID = 1L;
    private static final Long ID_CLIENTE = 2L;
    private static final Long ID_VEICULO = 10L;
    private static final String PLACA = "ABC1234";
    private static final String MARCA = "Fiat";
    private static final String MODELO = "Uno";
    private static final Integer ANO = 2020;

    // ===================== toDomain =====================

    @Test
    void deveConverterEntityParaDomainComClientePreenchido() {
        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaEntity.builder()
                .id(ID)
                .cliente(ClienteJpaEntity.builder().id(ID_CLIENTE).build())
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();

        ClienteVeiculo vinculo = ClienteVeiculoJpaMapper.toDomain(entity);

        assertThat(vinculo.getId()).isEqualTo(ID);
        assertThat(vinculo.getCliente().getId()).isEqualTo(ID_CLIENTE);
        assertThat(vinculo.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(vinculo.getPlaca()).isEqualTo(PLACA);
        assertThat(vinculo.getMarca()).isEqualTo(MARCA);
        assertThat(vinculo.getModelo()).isEqualTo(MODELO);
        assertThat(vinculo.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveConverterEntityParaDomainComClienteNulo() {
        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaEntity.builder()
                .id(ID)
                .veiculoId(ID_VEICULO)
                .build();

        ClienteVeiculo vinculo = ClienteVeiculoJpaMapper.toDomain(entity);

        assertThat(vinculo.getCliente()).isNull();
    }

    @Test
    void deveRetornarNuloAoConverterEntityNulaParaDomain() {
        assertThat(ClienteVeiculoJpaMapper.toDomain(null)).isNull();
    }

    // ===================== toJpaEntity =====================

    @Test
    void deveConverterDomainParaEntityComClientePreenchido() {
        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .id(ID)
                .cliente(Cliente.builder().id(ID_CLIENTE).build())
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();

        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaMapper.toJpaEntity(vinculo);

        assertThat(entity.getId()).isEqualTo(ID);
        assertThat(entity.getCliente().getId()).isEqualTo(ID_CLIENTE);
        assertThat(entity.getVeiculoId()).isEqualTo(ID_VEICULO);
        assertThat(entity.getPlaca()).isEqualTo(PLACA);
        assertThat(entity.getMarca()).isEqualTo(MARCA);
        assertThat(entity.getModelo()).isEqualTo(MODELO);
        assertThat(entity.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveConverterDomainParaEntityComClienteNulo() {
        ClienteVeiculo vinculo = ClienteVeiculo.builder()
                .id(ID)
                .veiculoId(ID_VEICULO)
                .build();

        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaMapper.toJpaEntity(vinculo);

        assertThat(entity.getCliente()).isNull();
    }

    @Test
    void deveRetornarNuloAoConverterDomainNuloParaEntity() {
        assertThat(ClienteVeiculoJpaMapper.toJpaEntity(null)).isNull();
    }
}
