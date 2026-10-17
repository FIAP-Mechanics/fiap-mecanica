package com.fiap.mecanica.atendimento.adapter.in.web.security;

import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.exception.OrdemServicoNaoEncontradaException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

public final class AcessoOrdemServico {

    private static final Set<String> PAPEIS_FUNCIONARIO = Set.of("ROLE_ADMIN", "ROLE_ATENDENTE", "ROLE_MECANICO");
    private static final String CLAIM_CLIENTE_ID = "cliente_id";

    private AcessoOrdemServico() {
    }

    public static void validarAcesso(OrdemServico ordemServico, Authentication authentication) {
        if (ehFuncionario(authentication)) {
            return;
        }
        Long clienteId = clienteIdDe(authentication);
        if (clienteId == null || !clienteId.equals(ordemServico.getClienteId())) {
            throw new OrdemServicoNaoEncontradaException(ordemServico.getId());
        }
    }

    public static Long clienteIdDe(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt && jwt.getClaim(CLAIM_CLIENTE_ID) instanceof Number numero) {
            return numero.longValue();
        }
        return null;
    }

    private static boolean ehFuncionario(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(PAPEIS_FUNCIONARIO::contains);
    }
}
