package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.domain.ServicoInsumo;
import com.fiap.mecanica.dto.ServicoDto;
import com.fiap.mecanica.exception.ServicoInativoException;
import com.fiap.mecanica.exception.ServicoNotFound;
import com.fiap.mecanica.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository repository;

    @InjectMocks
    private ServicoService service;

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

        assertThatThrownBy(() -> service.buscarServicoPorId(99L))
                .isInstanceOf(ServicoNotFound.class);
    }

    @Test
    void deveLancarExcecaoQuandoServicoEstiverInativo() {
        when(repository.findById(1L)).thenReturn(Optional.of(criarServico(false)));

        assertThatThrownBy(() -> service.buscarServicoPorId(1L))
                .isInstanceOf(ServicoInativoException.class);
    }

    @Test
    void deveAtualizarCamposEInsumos() {
        Servico servico = criarServico(true);
        ServicoInsumo novoInsumo = criarServicoInsumo("Oleo", "4.500");
        ServicoDto dto = new ServicoDto(null, "Troca de oleo", "Nova descricao",
                new BigDecimal("200.00"), List.of(novoInsumo));
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Servico resultado = service.atualizarServico(1L, dto);

        assertThat(resultado.getNome()).isEqualTo("Troca de oleo");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descricao");
        assertThat(resultado.getValor()).isEqualByComparingTo("200.00");
        assertThat(resultado.getInsumos()).containsExactly(novoInsumo);
        assertThat(novoInsumo.getServico()).isSameAs(servico);
    }

    @Test
    void deveManterCamposQuandoAtualizacaoForVazia() {
        Servico servico = criarServico(true);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizarServico(1L, new ServicoDto(null, null, null, null, null));

        assertThat(servico.getNome()).isEqualTo("Alinhamento");
        assertThat(servico.getInsumos()).hasSize(1);
    }

    @Test
    void deveLimparInsumosQuandoListaVaziaForInformada() {
        Servico servico = criarServico(true);
        when(repository.findById(1L)).thenReturn(Optional.of(servico));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.atualizarServico(1L, new ServicoDto(null, null, null, null, List.of()));

        assertThat(servico.getInsumos()).isEmpty();
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

    private Servico criarServico(boolean ativo) {
        Servico servico = Servico.builder()
                .id(1L)
                .nome("Alinhamento")
                .descricao("Alinhamento das rodas")
                .valor(new BigDecimal("100.00"))
                .ativo(ativo)
                .build();
        servico.atualizarInsumos(List.of(criarServicoInsumo("Pneu", "2")));
        return servico;
    }

    private ServicoInsumo criarServicoInsumo(String nome, String quantidade) {
        return ServicoInsumo.builder()
                .insumo(Insumo.builder().nome(nome).preco(new BigDecimal("450.00")).build())
                .quantidadeUtilizada(new BigDecimal(quantidade))
                .build();
    }
}
