package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteVeiculoJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository.ClienteVeiculoSpringDataRepository;
import com.fiap.mecanica.cliente.domain.Cliente;
import com.fiap.mecanica.cliente.domain.ClienteVeiculo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteVeiculoJpaGatewayTest {

    private static final Long ID_CLIENTE = 1L;
    private static final Long ID_VEICULO = 10L;
    private static final String PLACA = "ABC1234";
    private static final String MARCA = "Fiat";
    private static final String MODELO = "Uno";
    private static final Integer ANO = 2020;

    @Mock
    private ClienteVeiculoSpringDataRepository repository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ClienteVeiculoJpaGateway gateway;

    @Captor
    private ArgumentCaptor<ClienteVeiculoJpaEntity> entityCaptor;

    // ===================== existeVinculo =====================

    @Test
    void deveRetornarTrueQuandoVinculoJaExistir() {
        when(repository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(true);

        assertThat(gateway.existeVinculo(ID_CLIENTE, ID_VEICULO)).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoVinculoNaoExistir() {
        when(repository.existsByClienteIdAndVeiculoId(ID_CLIENTE, ID_VEICULO)).thenReturn(false);

        assertThat(gateway.existeVinculo(ID_CLIENTE, ID_VEICULO)).isFalse();
    }

    // ===================== buscarPorClienteId =====================

    @Test
    void deveRetornarVinculosDoClienteConvertidosParaDomain() {
        ClienteVeiculoJpaEntity entity = ClienteVeiculoJpaEntity.builder()
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();
        when(repository.findByClienteId(ID_CLIENTE)).thenReturn(List.of(entity));

        List<ClienteVeiculo> resultado = gateway.buscarPorClienteId(ID_CLIENTE);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getVeiculoId()).isEqualTo(ID_VEICULO);
    }

    @Test
    void deveRetornarListaVaziaQuandoClienteNaoPossuiVinculos() {
        when(repository.findByClienteId(ID_CLIENTE)).thenReturn(List.of());

        List<ClienteVeiculo> resultado = gateway.buscarPorClienteId(ID_CLIENTE);

        assertThat(resultado).isEmpty();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarVinculoUsandoReferenciaDoClienteQuandoIdPresente() {
        ClienteVeiculo vinculo = criarVinculo(Cliente.builder().id(ID_CLIENTE).build());
        ClienteJpaEntity referencia = ClienteJpaEntity.builder().id(ID_CLIENTE).build();
        when(entityManager.getReference(ClienteJpaEntity.class, ID_CLIENTE)).thenReturn(referencia);
        when(repository.save(entityCaptor.capture())).thenReturn(criarEntitySalva());

        ClienteVeiculo resultado = gateway.salvar(vinculo);

        assertThat(entityCaptor.getValue().getCliente()).isEqualTo(referencia);
        assertThat(resultado.getVeiculoId()).isEqualTo(ID_VEICULO);
        verify(entityManager).getReference(ClienteJpaEntity.class, ID_CLIENTE);
    }

    @Test
    void deveSalvarVinculoSemBuscarReferenciaQuandoClienteForNulo() {
        ClienteVeiculo vinculo = criarVinculo(null);
        when(repository.save(any())).thenReturn(criarEntitySalva());

        gateway.salvar(vinculo);

        verify(entityManager, never()).getReference(any(), any());
    }

    private ClienteVeiculo criarVinculo(Cliente cliente) {
        return ClienteVeiculo.builder()
                .cliente(cliente)
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();
    }

    private ClienteVeiculoJpaEntity criarEntitySalva() {
        return ClienteVeiculoJpaEntity.builder()
                .veiculoId(ID_VEICULO)
                .placa(PLACA)
                .marca(MARCA)
                .modelo(MODELO)
                .ano(ANO)
                .build();
    }
}
