package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.dto.VeiculoDto;
import com.fiap.mecanica.exception.ValidacaoException;
import com.fiap.mecanica.exception.VeiculoInativoException;
import com.fiap.mecanica.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.repository.VeiculoRepository;
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
class VeiculoServiceTest {

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
    private VeiculoRepository repository;

    @InjectMocks
    private VeiculoService service;

    @Captor
    private ArgumentCaptor<Veiculo> veiculoCaptor;

    @Test
    void deveCadastrarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.existsByPlaca(PLACA)).thenReturn(false);
        when(repository.save(any(Veiculo.class))).thenReturn(veiculo);

        Veiculo resultado = service.cadastrarVeiculo(veiculo);

        assertThat(resultado).isEqualTo(veiculo);

        verify(repository).existsByPlaca(PLACA);
        verify(repository).save(veiculo);
    }

    @Test
    void deveNormalizarPlacaAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setPlaca("abc1234");

        when(repository.existsByPlaca("ABC1234")).thenReturn(false);
        when(repository.save(veiculoCaptor.capture()))
                .thenReturn(veiculo);

        service.cadastrarVeiculo(veiculo);

        Veiculo capturado = veiculoCaptor.getValue();

        assertThat(capturado.getPlaca()).isEqualTo("ABC1234");
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoCadastrar() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.existsByPlaca(PLACA)).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(VeiculoJaCadastradoException.class)
                .hasMessage("Já existe um veículo cadastrado com a placa: " + PLACA);

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMenorQue1900() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(1899);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAnoForMaiorQuePermitido() {

        Veiculo veiculo = criarVeiculoAtivo();
        veiculo.setAno(Year.now().getValue() + 2);

        assertThatThrownBy(() -> service.cadastrarVeiculo(veiculo))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deveRetornarVeiculoQuandoBuscarPorIdExistente() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        Veiculo resultado = service.buscarVeiculoPorId(ID_EXISTENTE);

        assertThat(resultado).isEqualTo(veiculo);
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoBuscarPorId() {

        when(repository.findById(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarVeiculoPorId(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);
    }

    @Test
    void deveLancarVeiculoInativoExceptionAoBuscarVeiculoInativo() {

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoInativo()));

        assertThatThrownBy(() -> service.buscarVeiculoPorId(ID_EXISTENTE))
                .isInstanceOf(VeiculoInativoException.class)
                .hasMessage("Veículo inativo com ID: " + ID_EXISTENTE);
    }

    @Test
    void deveAtualizarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();
        VeiculoDto dto = criarDtoAtualizacao();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.existsByPlaca(PLACA_NOVA))
                .thenReturn(false);

        when(repository.save(any()))
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

        VeiculoDto dto = new VeiculoDto(
                null,
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA,
                ANO_NOVO
        );

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getPlaca()).isEqualTo(PLACA);

        verify(repository, never()).existsByPlaca(any());
    }

    @Test
    void deveLancarVeiculoJaCadastradoExceptionAoAtualizarComPlacaDuplicada() {

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        when(repository.existsByPlaca(PLACA_NOVA))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, criarDtoAtualizacao()))
                .isInstanceOf(VeiculoJaCadastradoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deveInativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoAtivo();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.excluirVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isFalse();
    }

    @Test
    void deveReativarVeiculoComSucesso() {

        Veiculo veiculo = criarVeiculoInativo();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.reativarVeiculo(ID_EXISTENTE);

        assertThat(resultado.isAtivo()).isTrue();
    }

    @Test
    void deveLancarValidacaoExceptionQuandoReativarVeiculoJaAtivo() {

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() -> service.reativarVeiculo(ID_EXISTENTE))
                .isInstanceOf(ValidacaoException.class)
                .hasMessage("O veículo já está ativo.");

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarVeiculoNaoEncontradoExceptionAoReativarVeiculoInexistente() {

        when(repository.findById(ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.reativarVeiculo(ID_INEXISTENTE))
                .isInstanceOf(VeiculoNaoEncontradoException.class)
                .hasMessage("Veículo não encontrado com ID: " + ID_INEXISTENTE);

        verify(repository, never()).save(any());
    }

    @Test
    void deveNormalizarPlacaAoAtualizar() {

        Veiculo veiculo = criarVeiculoAtivo();

        VeiculoDto dto = new VeiculoDto(
                null,
                null,
                null,
                "xyz9876",
                null
        );

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.existsByPlaca("XYZ9876"))
                .thenReturn(false);

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getPlaca()).isEqualTo("XYZ9876");
    }

    @Test
    void deveAtualizarVeiculoComDtoVazio() {

        Veiculo veiculo = criarVeiculoAtivo();

        VeiculoDto dto = VeiculoDto.builder().build();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getMarca()).isEqualTo(MARCA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);

        verify(repository, never()).existsByPlaca(any());
    }

    @Test
    void deveAtualizarSomenteMarca() {

        Veiculo veiculo = criarVeiculoAtivo();

        VeiculoDto dto = VeiculoDto.builder()
                .marca(MARCA_NOVA)
                .build();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(veiculo));

        when(repository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo resultado = service.atualizarVeiculo(ID_EXISTENTE, dto);

        assertThat(resultado.getMarca()).isEqualTo(MARCA_NOVA);
        assertThat(resultado.getModelo()).isEqualTo(MODELO);
        assertThat(resultado.getPlaca()).isEqualTo(PLACA);
        assertThat(resultado.getAno()).isEqualTo(ANO);
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMenorQue1900() {

        VeiculoDto dto = VeiculoDto.builder()
                .ano(1899)
                .build();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, dto))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarValidacaoExceptionQuandoAtualizarComAnoMaiorQuePermitido() {

        VeiculoDto dto = VeiculoDto.builder()
                .ano(Year.now().getValue() + 2)
                .build();

        when(repository.findById(ID_EXISTENTE))
                .thenReturn(Optional.of(criarVeiculoAtivo()));

        assertThatThrownBy(() ->
                service.atualizarVeiculo(ID_EXISTENTE, dto))
                .isInstanceOf(ValidacaoException.class);

        verify(repository, never()).save(any());
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

    private VeiculoDto criarDtoAtualizacao() {
        return new VeiculoDto(
                null,
                MARCA_NOVA,
                MODELO_NOVO,
                PLACA_NOVA,
                ANO_NOVO
        );
    }
}
