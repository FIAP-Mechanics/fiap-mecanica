package com.fiap.mecanica.estoque.adapter.in.web.presenter;

import com.fiap.mecanica.estoque.adapter.in.web.request.AtualizarInsumoRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.CadastrarEstoqueRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.CadastrarInsumoRequest;
import com.fiap.mecanica.estoque.adapter.in.web.request.DeduzirEstoqueItemRequest;
import com.fiap.mecanica.estoque.adapter.in.web.response.EstoqueDto;
import com.fiap.mecanica.estoque.application.command.AtualizarInsumoCommand;
import com.fiap.mecanica.estoque.application.command.DeduzirEstoqueItemCommand;
import com.fiap.mecanica.estoque.domain.Estoque;
import com.fiap.mecanica.estoque.domain.Insumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EstoquePresenterTest {

    private static final Long ID_INSUMO = 1L;
    private static final String NOME = "Oleo";
    private static final BigDecimal PRECO = new BigDecimal("45.90");
    private static final Long QUANTIDADE = 10L;

    @Test
    void deveConverterCadastrarRequestParaEntidade() {
        CadastrarEstoqueRequest request = CadastrarEstoqueRequest.builder()
                .insumo(CadastrarInsumoRequest.builder().nome(NOME).precoUnitario(PRECO).build())
                .quantidade(QUANTIDADE)
                .build();

        Estoque estoque = EstoquePresenter.toEntity(request);

        assertThat(estoque.getInsumo().getNome()).isEqualTo(NOME);
        assertThat(estoque.getInsumo().getPrecoUnitario()).isEqualTo(PRECO);
        assertThat(estoque.getQuantidadeInsumo()).isEqualTo(QUANTIDADE);
    }

    @Test
    void deveConverterAtualizarInsumoRequestParaCommand() {
        AtualizarInsumoRequest request = AtualizarInsumoRequest.builder().nome(NOME).precoUnitario(PRECO).build();

        AtualizarInsumoCommand command = EstoquePresenter.toCommand(request);

        assertThat(command.nome()).isEqualTo(NOME);
        assertThat(command.precoUnitario()).isEqualTo(PRECO);
    }

    @Test
    void deveConverterListaDeDeduzirRequestParaCommands() {
        List<DeduzirEstoqueItemRequest> itens = List.of(
                DeduzirEstoqueItemRequest.builder().insumoId(ID_INSUMO).quantidade(5L).build());

        List<DeduzirEstoqueItemCommand> commands = EstoquePresenter.toCommands(itens);

        assertThat(commands).hasSize(1);
        assertThat(commands.getFirst().insumoId()).isEqualTo(ID_INSUMO);
        assertThat(commands.getFirst().quantidade()).isEqualTo(5L);
    }

    @Test
    void deveConverterEstoqueParaDto() {
        Estoque estoque = Estoque.builder()
                .insumo(Insumo.builder().id(ID_INSUMO).nome(NOME).precoUnitario(PRECO).build())
                .quantidadeInsumo(QUANTIDADE)
                .build();

        EstoqueDto dto = EstoquePresenter.toDto(estoque);

        assertThat(dto.insumo().id()).isEqualTo(ID_INSUMO);
        assertThat(dto.insumo().nome()).isEqualTo(NOME);
        assertThat(dto.insumo().precoUnitario()).isEqualTo(PRECO);
        assertThat(dto.quantidadeInsumo()).isEqualTo(QUANTIDADE);
    }
}
