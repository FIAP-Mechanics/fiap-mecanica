package com.fiap.mecanica.adapter.in.web.controller;

import com.fiap.mecanica.adapter.in.web.request.AtualizarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarVeiculoRequest;
import com.fiap.mecanica.adapter.in.web.response.VeiculoDto;
import com.fiap.mecanica.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoControllerTest {

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
    private VeiculoUseCase service;

    @InjectMocks
    private VeiculoController controller;

    @Captor
    private ArgumentCaptor<Veiculo> veiculoCaptor;

    @Test
    void deveRetornarVeiculoDtoQuandoIdExistir() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(service.buscarVeiculoPorId(ID_EXISTENTE))
                .thenReturn(veiculo);

        VeiculoDto resultado = controller.get(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);
        assertThat(resultado.marca()).isEqualTo(MARCA);
        assertThat(resultado.modelo()).isEqualTo(MODELO);
        assertThat(resultado.placa()).isEqualTo(PLACA);
        assertThat(resultado.ano()).isEqualTo(ANO);

        verify(service).buscarVeiculoPorId(ID_EXISTENTE);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionNoGetQuandoIdNaoExistir() {

        when(service.buscarVeiculoPorId(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(service).buscarVeiculoPorId(ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoInativoExceptionNoGetQuandoVeiculoEstiverInativo() {

        when(service.buscarVeiculoPorId(ID_EXISTENTE))
                .thenThrow(new VeiculoInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.get(ID_EXISTENTE))
                .isInstanceOf(VeiculoInativoException.class)
                .hasMessage("Veículo inativo com ID: " + ID_EXISTENTE);

        verify(service).buscarVeiculoPorId(ID_EXISTENTE);
    }

    @Test
    void deveCadastrarVeiculoERetornarDtoComSucesso() {

        CadastrarVeiculoRequest request = criarCadastrarRequest();

        when(service.cadastrarVeiculo(any(Veiculo.class)))
                .thenReturn(criarVeiculoAtivo());

        VeiculoDto resultado = controller.create(request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);

        verify(service).cadastrarVeiculo(any(Veiculo.class));
    }

    @Test
    void deveConverterRequestParaEntidadeCorretamenteAoCadastrar() {

        when(service.cadastrarVeiculo(veiculoCaptor.capture()))
                .thenReturn(criarVeiculoAtivo());

        controller.create(criarCadastrarRequest());

        Veiculo capturado = veiculoCaptor.getValue();

        assertThat(capturado.getMarca()).isEqualTo(MARCA);
        assertThat(capturado.getModelo()).isEqualTo(MODELO);
        assertThat(capturado.getPlaca()).isEqualTo(PLACA);
        assertThat(capturado.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoCadastrarComPlacaDuplicada() {

        when(service.cadastrarVeiculo(any(Veiculo.class)))
                .thenThrow(new VeiculoJaCadastradoException(PLACA));

        assertThatThrownBy(() ->
                controller.create(criarCadastrarRequest()))
                .isInstanceOf(VeiculoJaCadastradoException.class);

        verify(service).cadastrarVeiculo(any(Veiculo.class));
    }

    @Test
    void deveAtualizarVeiculoERetornarDtoComSucesso() {

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), any(AtualizarVeiculoCommand.class)))
                .thenReturn(criarVeiculoAtualizado());

        VeiculoDto resultado =
                controller.update(ID_EXISTENTE, criarAtualizarRequestCompleto());

        assertThat(resultado.marca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.modelo()).isEqualTo(MODELO_NOVO);
        assertThat(resultado.placa()).isEqualTo(PLACA_NOVA);
        assertThat(resultado.ano()).isEqualTo(ANO_NOVO);

        verify(service).atualizarVeiculo(eq(ID_EXISTENTE), any(AtualizarVeiculoCommand.class));
    }

    @Test
    void deveConverterRequestParaDtoCorretamenteAoAtualizar() {

        ArgumentCaptor<AtualizarVeiculoCommand> commandCaptor =
                ArgumentCaptor.forClass(AtualizarVeiculoCommand.class);

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), commandCaptor.capture()))
                .thenReturn(criarVeiculoAtualizado());

        controller.update(ID_EXISTENTE, criarAtualizarRequestCompleto());

        AtualizarVeiculoCommand dto = commandCaptor.getValue();

        assertThat(dto.marca()).isEqualTo(MARCA_NOVA);
        assertThat(dto.modelo()).isEqualTo(MODELO_NOVO);
        assertThat(dto.placa()).isEqualTo(PLACA_NOVA);
        assertThat(dto.ano()).isEqualTo(ANO_NOVO);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionNoUpdateQuandoIdNaoExistir() {

        when(service.atualizarVeiculo(eq(ID_INEXISTENTE), any(AtualizarVeiculoCommand.class)))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() ->
                controller.update(ID_INEXISTENTE, criarAtualizarRequestCompleto()))
                .isInstanceOf(VeiculoNaoEncontradoException.class);
    }

    @Test
    void deveLancarVeiculoInativoExceptionNoUpdateQuandoVeiculoEstiverInativo() {

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), any(AtualizarVeiculoCommand.class)))
                .thenThrow(new VeiculoInativoException(ID_EXISTENTE));

        assertThatThrownBy(() ->
                controller.update(ID_EXISTENTE, criarAtualizarRequestCompleto()))
                .isInstanceOf(VeiculoInativoException.class);
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionNoUpdateComPlacaDuplicada() {

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), any(AtualizarVeiculoCommand.class)))
                .thenThrow(new VeiculoJaCadastradoException(PLACA_NOVA));

        assertThatThrownBy(() ->
                controller.update(ID_EXISTENTE, criarAtualizarRequestCompleto()))
                .isInstanceOf(VeiculoJaCadastradoException.class);
    }

    @Test
    void deveExcluirVeiculoLogicamenteERetornarDtoComSucesso() {

        when(service.excluirVeiculo(ID_EXISTENTE))
                .thenReturn(criarVeiculoInativo());

        VeiculoDto resultado = controller.delete(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);

        verify(service).excluirVeiculo(ID_EXISTENTE);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionNoDeleteQuandoIdNaoExistir() {

        when(service.excluirVeiculo(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class);
    }

    @Test
    void deveLancarVeiculoInativoExceptionNoDeleteQuandoVeiculoJaEstiverInativo() {

        when(service.excluirVeiculo(ID_EXISTENTE))
                .thenThrow(new VeiculoInativoException(ID_EXISTENTE));

        assertThatThrownBy(() -> controller.delete(ID_EXISTENTE))
                .isInstanceOf(VeiculoInativoException.class);
    }

    @Test
    void deveReativarVeiculoComSucesso() {

        when(service.reativarVeiculo(ID_EXISTENTE))
                .thenReturn(criarVeiculoAtivo());

        VeiculoDto resultado = controller.reativar(ID_EXISTENTE);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(ID_EXISTENTE);

        verify(service).reativarVeiculo(ID_EXISTENTE);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionNaReativacao() {

        when(service.reativarVeiculo(ID_INEXISTENTE))
                .thenThrow(new VeiculoNaoEncontradoException(ID_INEXISTENTE));

        assertThatThrownBy(() -> controller.reativar(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(service).reativarVeiculo(ID_INEXISTENTE);
    }

    @Test
    void deveLancarValidacaoExceptionQuandoVeiculoJaEstiverAtivoNaReativacao() {

        when(service.reativarVeiculo(ID_EXISTENTE))
                .thenThrow(new ValidacaoException("O veículo já está ativo."));

        assertThatThrownBy(() -> controller.reativar(ID_EXISTENTE))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("O veículo já está ativo.");

        verify(service).reativarVeiculo(ID_EXISTENTE);
    }

    @Test
    void deveAtualizarSomenteMarcaNoController() {

        AtualizarVeiculoRequest request =
                new AtualizarVeiculoRequest(MARCA_NOVA, null, null, null);

        Veiculo atualizado = Veiculo.builder()
                .id(ID_EXISTENTE)
                .marca(MARCA_NOVA)
                .modelo(MODELO)
                .placa(PLACA)
                .ano(ANO)
                .ativo(true)
                .build();

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), any()))
                .thenReturn(atualizado);

        VeiculoDto resultado =
                controller.update(ID_EXISTENTE, request);

        assertThat(resultado.marca()).isEqualTo(MARCA_NOVA);
    }

    @Test
    void deveAtualizarComRequestVazio() {

        AtualizarVeiculoRequest request =
                new AtualizarVeiculoRequest(null, null, null, null);

        when(service.atualizarVeiculo(eq(ID_EXISTENTE), any()))
                .thenReturn(criarVeiculoAtivo());

        VeiculoDto resultado =
                controller.update(ID_EXISTENTE, request);

        assertThat(resultado).isNotNull();

        verify(service).atualizarVeiculo(eq(ID_EXISTENTE), any());
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

    private Veiculo criarVeiculoAtualizado() {
        return Veiculo.builder()
                .id(ID_EXISTENTE)
                .marca(MARCA_NOVA)
                .modelo(MODELO_NOVO)
                .placa(PLACA_NOVA)
                .ano(ANO_NOVO)
                .ativo(true)
                .build();
    }

    private CadastrarVeiculoRequest criarCadastrarRequest() {
        return CadastrarVeiculoRequest.builder()
                .marca(MARCA)
                .modelo(MODELO)
                .placa(PLACA)
                .ano(ANO)
                .build();
    }

    private AtualizarVeiculoRequest criarAtualizarRequestCompleto() {
        return new AtualizarVeiculoRequest(
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA_NOVA,
                ANO_NOVO
        );
    }
}
