package com.fiap.mecanica.servico.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.servico.adapter.out.persistence.jpa.entity.ServicoJpaEntity;
import com.fiap.mecanica.servico.adapter.out.persistence.jpa.repository.ServicoSpringDataRepository;
import com.fiap.mecanica.servico.domain.Servico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoJpaGatewayTest {

    private static final Long ID = 1L;
    private static final String NOME = "Alinhamento";

    @Mock
    private ServicoSpringDataRepository repository;

    @InjectMocks
    private ServicoJpaGateway gateway;

    // ===================== buscarTodos =====================

    @Test
    void deveRetornarTodosOsServicosConvertidosParaDomain() {
        when(repository.findAll()).thenReturn(List.of(criarEntity()));

        List<Servico> resultado = gateway.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
        verify(repository).findAll();
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverServicos() {
        when(repository.findAll()).thenReturn(List.of());

        List<Servico> resultado = gateway.buscarTodos();

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarPorId =====================

    @Test
    void deveRetornarServicoQuandoIdExistir() {
        when(repository.findById(ID)).thenReturn(Optional.of(criarEntity()));

        Optional<Servico> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
    }

    @Test
    void deveRetornarOptionalVazioQuandoIdNaoExistir() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        Optional<Servico> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isEmpty();
    }

    // ===================== salvar =====================

    @Test
    void deveSalvarServicoERetornarDomainConvertido() {
        Servico servico = criarServico();
        when(repository.save(any())).thenReturn(criarEntity());

        Servico resultado = gateway.salvar(servico);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getNome()).isEqualTo(NOME);
    }

    private ServicoJpaEntity criarEntity() {
        return ServicoJpaEntity.builder()
                .id(ID)
                .nome(NOME)
                .valor(new BigDecimal("100.00"))
                .ativo(true)
                .build();
    }

    private Servico criarServico() {
        return Servico.builder()
                .id(ID)
                .nome(NOME)
                .valor(new BigDecimal("100.00"))
                .ativo(true)
                .build();
    }
}
