package com.fiap.mecanica.servico.service;

import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.dto.ServicoDto;
import com.fiap.mecanica.servico.exception.ServicoInativoException;
import com.fiap.mecanica.servico.exception.ServicoJaAtivoException;
import com.fiap.mecanica.servico.exception.ServicoNotFound;
import com.fiap.mecanica.servico.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private ServicoService service;

    @Test
    void deveBuscarTodosOsServicos() {
        Servico servico = criarServico(true);
        when(repository.findAll()).thenReturn(List.of(servico));

        List<Servico> resultado = service.buscarTodos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst()).isEqualTo(servico);
    }

    @Test
    void deveCadastrarServico() {
        Servico servico = criarServico(true);
        when(repository.save(servico)).thenReturn(servico);

        assertThat(service.cadastrarServico(servico)).isEqualTo(servico);
        verify(repository).save(servico);
    }

    @Test
    void deveBuscarServicoAtivo() {
        Servico servico = criarServico(true);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));

        assertThat(service.buscarServicoPorId(1L)).isEqualTo(servico);
    }

    @Test
    void deveLancarNotFoundQuandoServicoNaoExistir() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarServicoPorId(99L)).isInstanceOf(ServicoNotFound.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoEstiverInativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(criarServico(false)));

        assertThatThrownBy(() -> service.buscarServicoPorId(1L)).isInstanceOf(ServicoInativoException.class);
    }

    @Test
    void deveAtualizarCampos() {
        Servico servico = criarServico(true);
        ServicoDto dto = new ServicoDto(null, "Troca de oleo", "Nova descricao", new BigDecimal("200.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Servico resultado = service.atualizarServico(1L, dto);

        assertThat(resultado.getNome()).isEqualTo("Troca de oleo");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descricao");
        assertThat(resultado.getValor()).isEqualByComparingTo("200.00");
    }

    @Test
    void deveManterCamposQuandoAtualizacaoForVazia() {
        Servico servico = criarServico(true);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizarServico(1L, new ServicoDto(null, null, null, null));

        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getValor()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveRealizarDeleteLogico() {
        Servico servico = criarServico(true);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.excluirServico(1L).isAtivo()).isFalse();
    }

    @Test
    void naoDeveSalvarAoExcluirServicoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.excluirServico(99L)).isInstanceOf(ServicoNotFound.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deveReativarServicoInativo() {
        Servico servico = criarServico(false);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(servico)).thenReturn(servico);

        assertThat(service.reativarServico(1L).isAtivo()).isTrue();
    }

    @Test
    void deveImpedirReativacaoDeServicoAtivo() {
        when(repository.findById(1L)).thenReturn(Optional.of(criarServico(true)));

        assertThatThrownBy(() -> service.reativarServico(1L)).isInstanceOf(ServicoJaAtivoException.class);
    }

    private Servico criarServico(boolean ativo) {
        return Servico.builder()
                .id(1L)
                .nome("Alinhamento")
                .descricao("Alinhamento das rodas")
                .valor(new BigDecimal("100.00"))
                .ativo(ativo)
                .build();
    }
}
