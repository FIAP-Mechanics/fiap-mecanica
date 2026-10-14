package com.fiap.mecanica.atendimento.adapter.in.web.security;

import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.exception.OrdemServicoNaoEncontradaException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcessoOrdemServicoTest {

    private static final String ID_ORDEM = "550e8400-e29b-41d4-a716-446655440000";
    private static final Long ID_CLIENTE = 1L;
    private static final Long OUTRO_CLIENTE = 2L;

    // ===================== validarAcesso =====================

    @Test
    void devePermitirAcessoQuandoClienteForDonoDaOrdem() {
        assertThatCode(() -> AcessoOrdemServico.validarAcesso(criarOrdem(), criarAutenticacaoCliente(ID_CLIENTE)))
                .doesNotThrowAnyException();
    }

    @Test
    void deveLancarNaoEncontradaQuandoClienteNaoForDonoDaOrdem() {
        assertThatThrownBy(() -> AcessoOrdemServico.validarAcesso(criarOrdem(), criarAutenticacaoCliente(OUTRO_CLIENTE)))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class)
                .hasMessage("Ordem de serviço não encontrada com ID: " + ID_ORDEM);
    }

    @Test
    void deveLancarNaoEncontradaQuandoTokenNaoTiverClienteId() {
        Authentication semClienteId = new JwtAuthenticationToken(
                Jwt.withTokenValue("token").header("alg", "HS256").claim("sub", "x").build(),
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));

        assertThatThrownBy(() -> AcessoOrdemServico.validarAcesso(criarOrdem(), semClienteId))
                .isInstanceOf(OrdemServicoNaoEncontradaException.class);
    }

    @Test
    void devePermitirAcessoAQualquerOrdemQuandoForFuncionario() {
        for (String papel : List.of("ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO")) {
            Authentication funcionario = new UsernamePasswordAuthenticationToken(
                    "funcionario", null, List.of(new SimpleGrantedAuthority(papel)));

            assertThatCode(() -> AcessoOrdemServico.validarAcesso(criarOrdem(), funcionario))
                    .doesNotThrowAnyException();
        }
    }

    // ===================== clienteIdDe =====================

    @Test
    void deveRetornarClienteIdDoToken() {
        assertThat(AcessoOrdemServico.clienteIdDe(criarAutenticacaoCliente(ID_CLIENTE))).isEqualTo(ID_CLIENTE);
    }

    @Test
    void deveRetornarNuloQuandoPrincipalNaoForJwt() {
        Authentication autenticacao = new UsernamePasswordAuthenticationToken("usuario", null, List.of());

        assertThat(AcessoOrdemServico.clienteIdDe(autenticacao)).isNull();
    }

    private OrdemServico criarOrdem() {
        return OrdemServico.builder().id(ID_ORDEM).clienteId(ID_CLIENTE).build();
    }

    private Authentication criarAutenticacaoCliente(Long clienteId) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "HS256")
                .claim("sub", String.valueOf(clienteId))
                .claim("cliente_id", clienteId)
                .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
    }
}
