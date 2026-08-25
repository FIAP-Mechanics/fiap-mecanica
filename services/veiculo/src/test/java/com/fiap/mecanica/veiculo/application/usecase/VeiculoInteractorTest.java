package com.fiap.mecanica.veiculo.application.usecase;

import com.fiap.mecanica.veiculo.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.veiculo.application.port.out.VeiculoGateway;
import com.fiap.mecanica.veiculo.domain.Veiculo;
import com.fiap.mecanica.veiculo.exception.ValidacaoException;
import com.fiap.mecanica.veiculo.exception.VeiculoInativoException;
import com.fiap.mecanica.veiculo.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.veiculo.exception.VeiculoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoInteractorTest {

    private static final Long ID_EXISTENTE = 1L;
    private static final Long ID_INEXISTENTE = 99L;

    private static final String MARCA = "Fiat";
    private static final String MARCA_NOVA = "Volkswagen";

    private static final String MODELO = "Uno";
    private static final String MODELO_NOVO = "Gol";

    private static final String PLACA = "ABC1234";
    private static final String PLACA_NOVA = "XYZ9876";

    private static final Integer ANO = 2020;
    private static final Integer ANO_NOVO = 2023;

    @Mock
    private VeiculoGateway veiculoGateway;

    @InjectMocks
    private VeiculoInteractor interactor;

    @Captor
    private ArgumentCaptor<Veiculo> veiculoCaptor;

    @Test
    void deveCadastrarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(veiculoGateway.existsByPlaca(PLACA)).thenReturn(false);
        when(veiculoGateway.salvar(any(Veiculo.class))).thenReturn(veiculo);

        Veiculo resultado = interactor.cadastrarVeiculo(veiculo);

        assertThat(resultado).isEqualTo(veiculo);

        verify(veiculoGateway).existsByPlaca(PLACA);
        verify(veiculoGateway).salvar(veiculo);
    }

    @Test
    void deveNormalizarPlacaAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setPlaca("abc1234");

        when(veiculoGateway.existsByPlaca("ABC1234")).thenReturn(false);
        when(veiculoGateway.salvar(veiculoCaptor.capture()))
                .thenReturn(veiculo);

        interactor.cadastrarVeiculo(veiculo);

        Veiculo capturado = veiculoCaptor.getValue();

        assertThat(capturado.getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(veiculoGateway.existsByPlaca(PLACA)).thenReturn(true);

        assertThatThrownBy(() -> interactor.cadastrarVeiculo(veiculo))
                .isInstanceOf(VeiculoJaCadastradoException.class)
                .hasMessage("Já existe um veículo cadastrado com a placa: " + PLACA);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMenorQue1900() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(1899);

        assertThatThrownBy(() -> interactor.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMaiorQuePermitido() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(Year.now().getValue() + 2);

        assertThatThrownBy(() -> interactor.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveRetornarVeiculoQuandoBuscarPorIdExistente() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        Veiculo resultado = interactor.buscarVeiculoPorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(veiculo);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoBuscarPorId() {

        when(veiculoGateway.buscarPorId(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarVeiculoPorId(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoInativoExceptionAoBuscarVeiculoInativo() {

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoInativo()));

        assertThatThrownBy(() -> interactor.buscarVeiculoPorId(ID_EXISTENTE))
                .isInstanceOf(VeiculoInativoException.class)
                .hasMessage("Veículo inativo com ID: " + ID_EXISTENTE);
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();
        AtualizarVeiculoCommand command = criarCommandAtualizacao();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.existsByPlaca(PLACA_NOVA))
                .thenReturn(false);

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.atualizarVeiculo(ID_EXISTENTE, command);

        assertThat(resultado.getMarca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO_NOVO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA_NOVA);
        assertThat(resultado.getAno()).isEqualTo(ANO_NOVO);
    }

    @Test
    void devePermitirAtualizacaoSemAlterarPlaca() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA,
                ANO_NOVO
        );

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.atualizarVeiculo(ID_EXISTENTE, command);

        assertThat(resultado.getPlaca()).isEqualTo(PLACA);

        verify(veiculoGateway, never()).existsByPlaca(any());
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoAtualizarComPlacaDuplicada() {

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        when(veiculoGateway.existsByPlaca(PLACA_NOVA))
                .thenReturn(true);

        assertThatThrownBy(() ->
                interactor.atualizarVeiculo(ID_EXISTENTE, criarCommandAtualizacao()))
                .isInstanceOf(VeiculoJaCadastradoException.class);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveInativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.excluirVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isFalse();
    }

    @Test
    void deveReativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoInativo();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.reativarVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    void deveLancarValidacaoExceptionQuandoReativarVeiculoJaAtivo() {

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() -> interactor.reativarVeiculo(ID_EXISTENTE))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("O veículo já está ativo.");

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoReativarVeiculoInexistente() {

        when(veiculoGateway.buscarPorId(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                interactor.reativarVeiculo(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveNormalizarPlacaAoAtualizar() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                null,
                null,
                "xyz9876",
                null
        );

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.existsByPlaca("XYZ9876"))
                .thenReturn(false);

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.atualizarVeiculo(ID_EXISTENTE, command);

        assertThat(resultado.getPlaca()).isEqualTo("XYZ9876");
    }

    @Test
    void deveRetornarTodosOsVeiculos() {
        Veiculo veiculo = criarVeiculoAtivo();
        when(veiculoGateway.buscarTodos()).thenReturn(List.of(veiculo));

        List<Veiculo> resultado = interactor.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst()).isEqualTo(veiculo);
        verify(veiculoGateway).buscarTodos();
    }

    @Test
    void deveBuscarVeiculoPorPlacaComSucesso() {
        Veiculo veiculo = criarVeiculoAtivo();
        when(veiculoGateway.buscarPorPlaca(PLACA)).thenReturn(Optional.of(veiculo));

        Veiculo resultado = interactor.buscarPorPlaca(PLACA);

        assertThat(resultado).isEqualTo(veiculo);
        verify(veiculoGateway).buscarPorPlaca(PLACA);
    }

    @Test
    void deveLancarValidacaoExceptionAoBuscarPorPlacaInexistente() {
        when(veiculoGateway.buscarPorPlaca(PLACA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interactor.buscarPorPlaca(PLACA))
                .isInstanceOf(ValidacaoException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    void deveAtualizarVeiculoComCommandVazio() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand command = AtualizarVeiculoCommand.builder().build();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.atualizarVeiculo(ID_EXISTENTE, command);

        assertThat(resultado.getMarca()).isEqualTo(MARCA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);

        verify(veiculoGateway, never()).existsByPlaca(any());
    }

    @Test
    void deveAtualizarSomenteMarca() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand command = AtualizarVeiculoCommand.builder()
                .marca(MARCA_NOVA)
                .build();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(veiculoGateway.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = interactor.atualizarVeiculo(ID_EXISTENTE, command);

        assertThat(resultado.getMarca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMenorQue1900() {

        AtualizarVeiculoCommand command = AtualizarVeiculoCommand.builder()
                .ano(1899)
                .build();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                interactor.atualizarVeiculo(ID_EXISTENTE, command))
                .isInstanceOf(ValidacaoException.class);

        verify(veiculoGateway, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMaiorQuePermitido() {

        AtualizarVeiculoCommand command = AtualizarVeiculoCommand.builder()
                .ano(Year.now().getValue() + 2)
                .build();

        when(veiculoGateway.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                interactor.atualizarVeiculo(ID_EXISTENTE, command))
                .isInstanceOf(ValidacaoException.class);

        verify(veiculoGateway, never()).salvar(any());
    }

    private Veiculo criarVeiculoAtivo() {
        return Veiculo.builder()
                .id(ID_EXISTENTE)
                .marca(MARCA)
                .modelo(MODELO)
                .placa(PLACA)
                .ano(ANO)
                .ativo(true)
                .build();
    }

    private Veiculo criarVeiculoInativo() {
        return Veiculo.builder()
                .id(ID_EXISTENTE)
                .marca(MARCA)
                .modelo(MODELO)
                .placa(PLACA)
                .ano(ANO)
                .ativo(false)
                .build();
    }

    private AtualizarVeiculoCommand criarCommandAtualizacao() {
        return new AtualizarVeiculoCommand(
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA_NOVA,
                ANO_NOVO
        );
    }
}
