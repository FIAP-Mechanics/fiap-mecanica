package com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.gateway;

import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.entity.OrdemServicoJpaEntity;
import com.fiap.mecanica.atendimento.adapter.out.persistence.jpa.repository.OrdemServicoSpringDataRepository;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdemServicoJpaGatewayTest {

    private static final String ID = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private OrdemServicoSpringDataRepository repository;

    @InjectMocks
    private OrdemServicoJpaGateway gateway;

    // ===================== salvar =====================

    @Test
    void deveSalvarOrdemServicoERetornarDomainConvertido() {
        OrdemServico ordemServico = criarOrdemServico();
        when(repository.save(any())).thenReturn(criarEntity());

        OrdemServico resultado = gateway.salvar(ordemServico);

        assertThat(resultado.getId()).isEqualTo(ID);
        assertThat(resultado.getStatus()).isEqualTo(Status.RECEBIDA);
    }

    // ===================== buscarPorId =====================

    @Test
    void deveRetornarOrdemServicoQuandoIdExistir() {
        when(repository.findById(ID)).thenReturn(Optional.of(criarEntity()));

        Optional<OrdemServico> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(ID);
    }

    @Test
    void deveRetornarOptionalVazioQuandoIdNaoExistir() {
        when(repository.findById(ID)).thenReturn(Optional.empty());

        Optional<OrdemServico> resultado = gateway.buscarPorId(ID);

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarTodosPorStatusNotIn =====================

    @Test
    void deveRetornarOrdensDeServicoComStatusDiferenteDosInformados() {
        List<Status> statusExcluidos = List.of(Status.ENTREGUE, Status.CANCELADA);
        when(repository.findAllByStatusNotIn(statusExcluidos)).thenReturn(List.of(criarEntity()));

        List<OrdemServico> resultado = gateway.buscarTodosPorStatusNotIn(statusExcluidos);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
        verify(repository).findAllByStatusNotIn(statusExcluidos);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverOrdensComStatusDiferente() {
        List<Status> statusExcluidos = List.of(Status.ENTREGUE);
        when(repository.findAllByStatusNotIn(statusExcluidos)).thenReturn(List.of());

        List<OrdemServico> resultado = gateway.buscarTodosPorStatusNotIn(statusExcluidos);

        assertThat(resultado).isEmpty();
    }

    // ===================== buscarTodosPorStatusIn =====================

    @Test
    void deveRetornarOrdensDeServicoComStatusInformado() {
        List<Status> statusIncluidos = List.of(Status.FINALIZADA);
        when(repository.findAllByStatusIn(statusIncluidos)).thenReturn(List.of(criarEntity()));

        List<OrdemServico> resultado = gateway.buscarTodosPorStatusIn(statusIncluidos);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getId()).isEqualTo(ID);
        verify(repository).findAllByStatusIn(statusIncluidos);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverOrdensComStatusInformado() {
        List<Status> statusIncluidos = List.of(Status.FINALIZADA);
        when(repository.findAllByStatusIn(statusIncluidos)).thenReturn(List.of());

        List<OrdemServico> resultado = gateway.buscarTodosPorStatusIn(statusIncluidos);

        assertThat(resultado).isEmpty();
    }

    private OrdemServicoJpaEntity criarEntity() {
        return OrdemServicoJpaEntity.builder()
                .id(ID)
                .status(Status.RECEBIDA)
                .build();
    }

    private OrdemServico criarOrdemServico() {
        return OrdemServico.builder()
                .id(ID)
                .status(Status.RECEBIDA)
                .build();
    }
}
