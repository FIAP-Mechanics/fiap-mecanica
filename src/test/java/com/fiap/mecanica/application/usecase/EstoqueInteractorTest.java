package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.application.exception.TemplateNotFound;
import com.fiap.mecanica.application.port.out.EstoqueGateway;
import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.domain.Estoque;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.OrdemServicoInsumo;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.exception.EstoqueInativoException;
import com.fiap.mecanica.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.exception.EstoqueJaAtivoException;
import com.fiap.mecanica.exception.EstoqueNotFound;
import com.fiap.mecanica.support.TransacaoImediata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueInteractorTest {
    @Mock
    private EstoqueGateway repository;

    @Mock
    private NotificacaoGateway notificationService;

    private EstoqueInteractor service;

    @BeforeEach
    void setUp() {
        service = new EstoqueInteractor(repository, notificationService, new TransacaoImediata());
    }

    @Test
    void deveCadastrarRegistroNoEstoque() {
        Estoque estoque = criarEstoque(true);
        when(repository.salvar(estoque)).thenReturn(estoque);

        assertThat(service.cadastrarEstoque(estoque)).isSameAs(estoque);
    }

    @Test
    void deveBuscarEstoquePeloIdDoInsumo() {
        Estoque estoque = criarEstoque(true);
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));

        assertThat(service.buscarPorIdInsumo(1L)).isSameAs(estoque);
    }

    @Test
    void deveListarSomenteRegistrosAtivosDoEstoque() {
        Estoque estoque = criarEstoque(true);
        when(repository.buscarAtivos()).thenReturn(List.of(estoque));

        assertThat(service.listarEstoque()).containsExactly(estoque);
    }

    @Test
    void deveRejeitarEstoqueInexistenteOuInativo() {
        when(repository.buscarPorInsumoId(99L)).thenReturn(Optional.empty());
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(criarEstoque(false)));

        assertThatThrownBy(() -> service.buscarPorIdInsumo(99L)).isInstanceOf(EstoqueNotFound.class);
        assertThatThrownBy(() -> service.buscarPorIdInsumo(1L)).isInstanceOf(EstoqueInativoException.class);
    }

    @Test
    void deveAtualizarQuantidade() {
        Estoque estoque = criarEstoque(true);
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque resultado = service.atualizarQuantidade(1L, 25L);

        assertThat(resultado.getQuantidadeInsumo()).isEqualTo(25L);
    }

    @Test
    void deveAtualizarDadosDoInsumo() {
        Estoque estoque = criarEstoque(true);
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Estoque resultado = service.atualizarInsumo(
                1L, new AtualizarInsumoCommand("Filtro", new BigDecimal("30.00")));

        assertThat(resultado.getInsumo().getNome()).isEqualTo("Filtro");
        assertThat(resultado.getInsumo().getPrecoUnitario()).isEqualByComparingTo("30.00");
    }

    @Test
    void deveExcluirEReativarEstoque() {
        Estoque estoque = criarEstoque(true);
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.excluirEstoque(1L).isAtivo()).isFalse();
        assertThat(service.reativarEstoque(1L).isAtivo()).isTrue();
    }

    @Test
    void deveImpedirReativacaoDeEstoqueAtivo() {
        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(criarEstoque(true)));

        assertThatThrownBy(() -> service.reativarEstoque(1L)).isInstanceOf(EstoqueJaAtivoException.class);
    }

    // ===================== deduzirEstoque =====================

    @Test
    void deveDeduzirEstoqueComSucesso() {
        // Arrange
        Insumo insumo = Insumo.builder().id(1L).nome("Óleo").build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(2)
                .build();
        Estoque estoque = criarEstoque(true); // Inicialmente com 10L
        estoque.setQuantidadeInsumo(10L);

        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));
        when(repository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.deduzirEstoque(List.of(osInsumo));

        // Assert
        assertThat(estoque.getQuantidadeInsumo()).isEqualTo(8L);
        verify(repository).salvar(estoque);
        verifyNoInteractions(notificationService);
    }

    @Test
    void deveLancarEstoqueInsuficienteExceptionQuandoQuantidadeForMaiorQueDisponivel() {
        // Arrange
        Insumo insumo = Insumo.builder().id(1L).nome("Óleo").build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(15)
                .build();
        Estoque estoque = criarEstoque(true); // Inicialmente com 10L

        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));

        // Act & Assert
        assertThatThrownBy(() -> service.deduzirEstoque(List.of(osInsumo)))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("Estoque insuficiente para o insumo 'Óleo'. Disponível: 10, Solicitado: 15");

        verify(notificationService).notificarFuncionarios(
                CodigoTemplate.REPOSICAO_ESTOQUE,
                osInsumo.getInsumo().getNome(),
                "10",
                "15"
        );
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveManterErroDeEstoqueInsuficienteQuandoNotificacaoFalhar() {
        // Arrange
        Insumo insumo = Insumo.builder().id(1L).nome("Oleo").build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(15)
                .build();
        Estoque estoque = criarEstoque(true);

        when(repository.buscarPorInsumoId(1L)).thenReturn(Optional.of(estoque));
        doThrow(new TemplateNotFound(CodigoTemplate.REPOSICAO_ESTOQUE))
                .when(notificationService)
                .notificarFuncionarios(eq(CodigoTemplate.REPOSICAO_ESTOQUE), any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> service.deduzirEstoque(List.of(osInsumo)))
                .isInstanceOf(EstoqueInsuficienteException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarEstoqueNotFoundAoDeduzirEstoqueDeInsumoNaoCadastrado() {
        // Arrange
        Insumo insumo = Insumo.builder().id(99L).build();
        OrdemServicoInsumo osInsumo = OrdemServicoInsumo.builder()
                .insumo(insumo)
                .quantidade(1)
                .build();

        when(repository.buscarPorInsumoId(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> service.deduzirEstoque(List.of(osInsumo)))
                .isInstanceOf(EstoqueNotFound.class);
    }

    private Estoque criarEstoque(boolean ativo) {
        return Estoque.builder()
                .id(10L)
                .insumo(Insumo.builder()
                        .id(1L)
                        .nome("Oleo")
                        .precoUnitario(new BigDecimal("45.90"))
                        .build())
                .quantidadeInsumo(10L)
                .ativo(ativo)
                .build();
    }
}
