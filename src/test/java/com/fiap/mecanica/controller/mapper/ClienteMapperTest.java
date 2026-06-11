package com.fiap.mecanica.controller.mapper;

import com.fiap.mecanica.controller.request.AtualizarClienteRequest;
import com.fiap.mecanica.controller.request.CadastrarClienteRequest;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.Endereco;
import com.fiap.mecanica.dto.ClienteDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

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

    // --- toEntity (CadastrarClienteRequest) ---

    @Test
    void deveConverterCadastrarRequestParaEntidadeRemovendoMascaras() {
        CadastrarClienteRequest request = CadastrarClienteRequest.builder()
                .nome(NOME)
                .documento("123.456.789-99")
                .email(EMAIL)
                .telefone("(31) 9 9800-0000")
                .endereco(enderecoFormatado())
                .build();

        Cliente cliente = ClienteMapper.toEntity(request);

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

        Cliente cliente = ClienteMapper.toEntity(request);

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

        Cliente cliente = ClienteMapper.toEntity(request);

        assertThat(cliente.getEndereco().getEstado()).isEqualTo("MG");
        assertThat(cliente.getEndereco().getCidade()).isEqualTo("Belo Horizonte");
        assertThat(cliente.getEndereco().getBairro()).isEqualTo("Centro");
        assertThat(cliente.getEndereco().getRua()).isEqualTo("Rua A");
        assertThat(cliente.getEndereco().getNumero()).isEqualTo("10");
        assertThat(cliente.getEndereco().getComplemento()).isEqualTo("Ap 1");
    }

    // --- toDto (AtualizarClienteRequest) ---

    @Test
    void deveConverterAtualizarRequestParaDtoRemovendoMascaras() {
        AtualizarClienteRequest request = new AtualizarClienteRequest(
                NOME, "123.456.789-99", EMAIL, "(31) 9 9800-0000", enderecoFormatado());

        ClienteDto dto = ClienteMapper.toDto(request);

        assertThat(dto.nome()).isEqualTo(NOME);
        assertThat(dto.documento()).isEqualTo(DOCUMENTO_LIMPO);
        assertThat(dto.telefone()).isEqualTo(TELEFONE_LIMPO);
        assertThat(dto.endereco().getCep()).isEqualTo(CEP_LIMPO);
        assertThat(dto.email()).isEqualTo(EMAIL);
        assertThat(dto.id()).isNull();
    }

    @Test
    void deveConverterAtualizarRequestComCamposNulos() {
        AtualizarClienteRequest request = new AtualizarClienteRequest(null, null, null, null, null);

        ClienteDto dto = ClienteMapper.toDto(request);

        assertThat(dto.nome()).isNull();
        assertThat(dto.documento()).isNull();
        assertThat(dto.telefone()).isNull();
        assertThat(dto.endereco()).isNull();
    }

    // --- toDto (Cliente) ---

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

        ClienteDto dto = ClienteMapper.toDto(cliente);

        assertThat(dto.id()).isEqualTo(ID);
        assertThat(dto.nome()).isEqualTo(NOME);
        assertThat(dto.documento()).isEqualTo(DOCUMENTO_LIMPO);
        assertThat(dto.email()).isEqualTo(EMAIL);
        assertThat(dto.telefone()).isEqualTo(TELEFONE_LIMPO);
        assertThat(dto.endereco().getCep()).isEqualTo(CEP_LIMPO);
    }

    // --- apenasDigitos ---

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource({
            "123.456.789-00, 12345678900",
            "12.345.678/0001-99, 12345678000199",
            "(31) 9 9800-0000, 31998000000",
            "30000-000, 30000000",
            "  123  , 123",
            "12345, 12345",
            "'', ''"
    })
    void deveRemoverCaracteresNaoNumericos(String entrada, String esperado) {
        assertThat(ClienteMapper.apenasDigitos(entrada)).isEqualTo(esperado);
    }

    @Test
    void deveRetornarNuloQuandoEntradaForNula() {
        assertThat(ClienteMapper.apenasDigitos(null)).isNull();
    }
}
