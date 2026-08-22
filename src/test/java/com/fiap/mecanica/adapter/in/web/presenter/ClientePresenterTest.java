package com.fiap.mecanica.adapter.in.web.presenter;

import com.fiap.mecanica.adapter.in.web.request.AtualizarClienteRequest;
import com.fiap.mecanica.adapter.in.web.request.CadastrarClienteRequest;
import com.fiap.mecanica.adapter.in.web.response.ClienteDto;
import com.fiap.mecanica.application.command.AtualizarClienteCommand;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientePresenterTest {

    private static final Long ID = 1L;
    private static final String NOME = "José da Silva";
    private static final String DOCUMENTO_LIMPO = "12345678999";
    private static final String EMAIL = "cliente@email.com";
    private static final String TELEFONE_LIMPO = "31998000000";
    private static final String CEP_LIMPO = "30000000";

    private static Endereco enderecoFormatado() {
        return Endereco.builder()
                .cep("30000-000")
                .estado("MG").cidade("Belo Horizonte")
                .bairro("Centro").rua("Rua A").numero("10").complemento("Ap 1")
                .build();
    }

    private static Endereco enderecoLimpo() {
        return Endereco.builder()
                .cep(CEP_LIMPO)
                .estado("MG").cidade("Belo Horizonte")
                .bairro("Centro").rua("Rua A").numero("10").complemento("Ap 1")
                .build();
    }

    @Test
    void deveConverterCadastrarRequestParaEntidadeRemovendoMascaras() {
        CadastrarClienteRequest request = CadastrarClienteRequest.builder()
                .nome(NOME)
                .documento("123.456.789-99")
                .email(EMAIL)
                .telefone("(31) 9 9800-0000")
                .endereco(enderecoFormatado())
                .build();

        Cliente cliente = ClientePresenter.toEntity(request);

        assertThat(cliente.getNome()).isEqualTo(NOME);
        assertThat(cliente.getDocumento()).isEqualTo(DOCUMENTO_LIMPO);
        assertThat(cliente.getTelefone()).isEqualTo(TELEFONE_LIMPO);
        assertThat(cliente.getEndereco().getCep()).isEqualTo(CEP_LIMPO);
        assertThat(cliente.getEmail()).isEqualTo(EMAIL);
        assertThat(cliente.getId()).isNull();
    }

    @Test
    void deveConverterCadastrarRequestComCnpjFormatado() {
        CadastrarClienteRequest request = CadastrarClienteRequest.builder()
                .nome(NOME)
                .documento("12.345.678/0001-99")
                .email(EMAIL)
                .telefone(TELEFONE_LIMPO)
                .endereco(enderecoLimpo())
                .build();

        Cliente cliente = ClientePresenter.toEntity(request);

        assertThat(cliente.getDocumento()).isEqualTo("12345678000199");
    }

    @Test
    void devePreservarCamposDeEnderecoQueNaoSaoNumericos() {
        CadastrarClienteRequest request = CadastrarClienteRequest.builder()
                .nome(NOME)
                .documento(DOCUMENTO_LIMPO)
                .email(EMAIL)
                .telefone(TELEFONE_LIMPO)
                .endereco(enderecoFormatado())
                .build();

        Cliente cliente = ClientePresenter.toEntity(request);

        assertThat(cliente.getEndereco().getEstado()).isEqualTo("MG");
        assertThat(cliente.getEndereco().getCidade()).isEqualTo("Belo Horizonte");
        assertThat(cliente.getEndereco().getBairro()).isEqualTo("Centro");
        assertThat(cliente.getEndereco().getRua()).isEqualTo("Rua A");
        assertThat(cliente.getEndereco().getNumero()).isEqualTo("10");
        assertThat(cliente.getEndereco().getComplemento()).isEqualTo("Ap 1");
    }

    @Test
    void deveConverterAtualizarRequestParaCommandRemovendoMascaras() {
        AtualizarClienteRequest request = new AtualizarClienteRequest(
                NOME, "123.456.789-99", EMAIL, "(31) 9 9800-0000", enderecoFormatado());

        AtualizarClienteCommand command = ClientePresenter.toCommand(request);

        assertThat(command.nome()).isEqualTo(NOME);
        assertThat(command.documento()).isEqualTo(DOCUMENTO_LIMPO);
        assertThat(command.telefone()).isEqualTo(TELEFONE_LIMPO);
        assertThat(command.endereco().getCep()).isEqualTo(CEP_LIMPO);
        assertThat(command.email()).isEqualTo(EMAIL);
    }

    @Test
    void deveConverterClienteParaDtoSemAlterarValores() {
        Cliente cliente = Cliente.builder()
                .id(ID)
                .nome(NOME)
                .documento(DOCUMENTO_LIMPO)
                .email(EMAIL)
                .telefone(TELEFONE_LIMPO)
                .endereco(enderecoLimpo())
                .build();

        ClienteDto dto = ClientePresenter.toDto(cliente);

        assertThat(dto.id()).isEqualTo(ID);
        assertThat(dto.nome()).isEqualTo(NOME);
        assertThat(dto.documento()).isEqualTo(DOCUMENTO_LIMPO);
        assertThat(dto.email()).isEqualTo(EMAIL);
        assertThat(dto.telefone()).isEqualTo(TELEFONE_LIMPO);
        assertThat(dto.endereco().getCep()).isEqualTo(CEP_LIMPO);
    }
}
