package com.fiap.mecanica.cliente.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.entity.ClienteJpaEntity;
import com.fiap.mecanica.cliente.adapter.out.persistence.jpa.repository.ClienteSpringDataRepository;
import com.fiap.mecanica.cliente.domain.Cliente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteJpaGatewayTest {

    private static final Long ID = 1L;
    private static final String NOME = "José da Silva";
    private static final String DOCUMENTO = "12345678900";

    @Mock
    private ClienteSpringDataRepository repository;

    @InjectMocks
    private ClienteJpaGateway gateway;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarTodosOsClientesConvertidosParaDomain() {
        when(repository.findAll()).thenReturn(List.of(criarEntity()));

        List<Cliente> resultado = gateway.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
        verify(repository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverClientes() {
        when(repository.findAll()).thenReturn(List.of());

        List<Cliente> resultado = gateway.buscarTodos();

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorId =====================

    @Test
    void deveRetornarClienteQuandoIdExistir() {
        when(repository.findById(ID)).thenReturn(Optional.of(criarEntity()));

        Optional<Cliente> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
    }

    @Test
    void deveRetornarOptionalVazioQuandoIdNaoExistir() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        Optional<Cliente> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorDocumento =====================

    @Test
    void deveRetornarClienteQuandoDocumentoExistir() {
        when(repository.findByDocumento(DOCUMENTO)).thenReturn(Optional.of(criarEntity()));

        Optional<Cliente> resultado = gateway.buscarPorDocumento(DOCUMENTO);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getDocumento()).isEqualTo(DOCUMENTO);
    }

    @Test
    void deveRetornarOptionalVazioQuandoDocumentoNaoExistir() {
        when(repository.findByDocumento(DOCUMENTO)).thenReturn(Optional.empty());

        Optional<Cliente> resultado = gateway.buscarPorDocumento(DOCUMENTO);

        assertThat(resultado).isEmpty();
    }

    // ===================== existePorDocumento =====================

    @Test
    void deveRetornarTrueQuandoDocumentoJaExistir() {
        when(repository.existsByDocumento(DOCUMENTO)).thenReturn(true);

        assertThat(gateway.existePorDocumento(DOCUMENTO)).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoDocumentoNaoExistir() {
        when(repository.existsByDocumento(DOCUMENTO)).thenReturn(false);

        assertThat(gateway.existePorDocumento(DOCUMENTO)).isFalse();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarClienteERetornarDomainConvertido() {
        Cliente cliente = criarCliente();
        when(repository.save(any())).thenReturn(criarEntity());

        Cliente resultado = gateway.salvar(cliente);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }

    private ClienteJpaEntity criarEntity() {
        return ClienteJpaEntity.builder()
                .id(ID)
                .nome(NOME)
                .documento(DOCUMENTO)
                .build();
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .id(ID)
                .nome(NOME)
                .documento(DOCUMENTO)
                .build();
    }
}
