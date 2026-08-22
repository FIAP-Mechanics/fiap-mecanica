package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.ValidacaoException;
import com.fiap.mecanica.exception.VeiculoInativoException;
import com.fiap.mecanica.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private VeiculoGateway repository;

    @InjectMocks
    private VeiculoInteractor service;

    @Captor
    private ArgumentCaptor<Veiculo> veiculoCaptor;

    @Test
    void deveCadastrarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.existePorPlaca(PLACA)).thenReturn(false);
        when(repository.salvar(any(Veiculo.class))).thenReturn(veiculo);

        Veiculo resultado = service.cadastrarVeiculo(veiculo);

        assertThat(resultado).isEqualTo(veiculo);

        verify(repository).existePorPlaca(PLACA);
        verify(repository).salvar(veiculo);
    }

    @Test
    void deveNormalizarPlacaAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setPlaca("abc1234");

        when(repository.existePorPlaca("ABC1234")).thenReturn(false);
        when(repository.salvar(veiculoCaptor.capture()))
                .thenReturn(veiculo);

        service.cadastrarVeiculo(veiculo);

        Veiculo capturado = veiculoCaptor.getValue();

        assertThat(capturado.getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.existePorPlaca(PLACA)).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(VeiculoJaCadastradoException.class)
                .hasMessage("Já existe um veículo cadastrado com a placa: " + PLACA);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMenorQue1900() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(1899);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMaiorQuePermitido() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(Year.now().getValue() + 2);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveRetornarVeiculoQuandoBuscarPorIdExistente() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        Veiculo resultado = service.buscarVeiculoPorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(veiculo);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoBuscarPorId() {

        when(repository.buscarPorId(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarVeiculoPorId(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoInativoExceptionAoBuscarVeiculoInativo() {

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoInativo()));

        assertThatThrownBy(() -> service.buscarVeiculoPorId(ID_EXISTENTE))
                .isInstanceOf(VeiculoInativoException.class)
                .hasMessage("Veículo inativo com ID: " + ID_EXISTENTE);
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();
        AtualizarVeiculoCommand dto = criarDtoAtualizacao();

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.existePorPlaca(PLACA_NOVA))
                .thenReturn(false);

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getMarca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO_NOVO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA_NOVA);
        assertThat(resultado.getAno()).isEqualTo(ANO_NOVO);
    }

    @Test
    void devePermitirAtualizacaoSemAlterarPlaca() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand dto = new AtualizarVeiculoCommand(
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA,
                ANO_NOVO
        );

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getPlaca()).isEqualTo(PLACA);

        verify(repository, never()).existePorPlaca(any());
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoAtualizarComPlacaDuplicada() {

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        when(repository.existePorPlaca(PLACA_NOVA))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, criarDtoAtualizacao()))
                .isInstanceOf(VeiculoJaCadastradoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveInativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.excluirVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isFalse();
    }

    @Test
    void deveReativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoInativo();

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.reativarVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    void deveLancarValidacaoExceptionQuandoReativarVeiculoJaAtivo() {

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() -> service.reativarVeiculo(ID_EXISTENTE))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("O veículo já está ativo.");

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoReativarVeiculoInexistente() {

        when(repository.buscarPorId(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.reativarVeiculo(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveNormalizarPlacaAoAtualizar() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand dto = new AtualizarVeiculoCommand(
                null,
                null,
                "xyz9876",
                null
        );

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.existePorPlaca("XYZ9876"))
                .thenReturn(false);

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getPlaca()).isEqualTo("XYZ9876");
    }

    @Test
    void deveAtualizarVeiculoComDtoVazio() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand dto = new AtualizarVeiculoCommand(null, null, null, null);

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getMarca()).isEqualTo(MARCA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);

        verify(repository, never()).existePorPlaca(any());
    }

    @Test
    void deveAtualizarSomenteMarca() {

        Veiculo veiculo = criarVeiculoAtivo();

        AtualizarVeiculoCommand dto =
                new AtualizarVeiculoCommand(MARCA_NOVA, null, null, null);

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.salvar(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getMarca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMenorQue1900() {

        AtualizarVeiculoCommand dto =
                new AtualizarVeiculoCommand(null, null, null, 1899);

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, dto))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMaiorQuePermitido() {

        AtualizarVeiculoCommand dto = new AtualizarVeiculoCommand(
                null, null, null, Year.now().getValue() + 2);

        when(repository.buscarPorId(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, dto))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).salvar(any());
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

    private AtualizarVeiculoCommand criarDtoAtualizacao() {
        return new AtualizarVeiculoCommand(
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA_NOVA,
                ANO_NOVO
        );
    }
}
