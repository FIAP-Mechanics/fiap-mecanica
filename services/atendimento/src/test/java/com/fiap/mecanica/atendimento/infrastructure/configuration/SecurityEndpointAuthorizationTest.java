package com.fiap.mecanica.atendimento.infrastructure.configuration;

import com.fiap.mecanica.atendimento.adapter.in.web.controller.AtendimentoController;
import com.fiap.mecanica.atendimento.adapter.in.web.controller.TemplateController;
import com.fiap.mecanica.atendimento.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.atendimento.application.port.in.TemplateUseCase;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AtendimentoController.class, TemplateController.class})
@Import({SecurityConfiguration.class, SecurityEndpointAuthorizationTest.MockConfig.class})
@TestPropertySource(properties = "security.jwt.secret=0123456789abcdef0123456789abcdef")
class SecurityEndpointAuthorizationTest {

    private static final String ORDEM_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AtendimentoUseCase atendimentoUseCase;
    @Autowired
    private TemplateUseCase templateUseCase;

    @BeforeEach
    void setUp() {
        reset(atendimentoUseCase, templateUseCase);
    }

    @Test
    void atendentePodeIniciarAtendimento() throws Exception {
        when(atendimentoUseCase.iniciarAtendimento(eq(1L), eq(2L), eq("Nao liga"), any(), any()))
                .thenReturn(ordem(Status.RECEBIDA));

        mockMvc.perform(post("/atendimento/iniciar")
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": 1,
                                  "veiculo": 2,
                                  "relatoCliente": "Nao liga"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void listagemDeAtendimentosAbertosExigeToken() throws Exception {
        mockMvc.perform(get("/atendimento/abertos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultaDeStatusExigeToken() throws Exception {
        mockMvc.perform(get("/atendimento/{id}/status", ORDEM_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePodeConsultarStatusDaPropriaOrdem() throws Exception {
        when(atendimentoUseCase.buscarPorId(ORDEM_ID)).thenReturn(ordemDoCliente(1L));

        mockMvc.perform(get("/atendimento/{id}/status", ORDEM_ID).with(tokenDoCliente(1L)))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoPodeConsultarStatusDeOrdemDeOutroCliente() throws Exception {
        when(atendimentoUseCase.buscarPorId(ORDEM_ID)).thenReturn(ordemDoCliente(1L));

        mockMvc.perform(get("/atendimento/{id}/status", ORDEM_ID).with(tokenDoCliente(2L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void funcionarioPodeConsultarStatusDeQualquerOrdem() throws Exception {
        when(atendimentoUseCase.buscarPorId(ORDEM_ID)).thenReturn(ordemDoCliente(1L));

        mockMvc.perform(get("/atendimento/{id}/status", ORDEM_ID).with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isOk());
    }

    @Test
    void decisaoDoOrcamentoExigeToken() throws Exception {
        mockMvc.perform(post("/atendimento/{id}/decisao-orcamento", ORDEM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePodeRegistrarDecisaoDoOrcamentoDaPropriaOrdem() throws Exception {
        when(atendimentoUseCase.buscarPorId(ORDEM_ID)).thenReturn(ordemDoCliente(1L));
        when(atendimentoUseCase.aprovarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.EM_EXECUCAO));

        mockMvc.perform(post("/atendimento/{id}/decisao-orcamento", ORDEM_ID)
                        .with(tokenDoCliente(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoPodeRegistrarDecisaoDoOrcamentoDeOutroCliente() throws Exception {
        when(atendimentoUseCase.buscarPorId(ORDEM_ID)).thenReturn(ordemDoCliente(1L));

        mockMvc.perform(post("/atendimento/{id}/decisao-orcamento", ORDEM_ID)
                        .with(tokenDoCliente(2L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void apenasClientePodeListarSuasOrdens() throws Exception {
        when(atendimentoUseCase.listarAtendimentosDoCliente(1L)).thenReturn(List.of(ordemDoCliente(1L)));

        mockMvc.perform(get("/atendimento/cliente/minhas"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/atendimento/cliente/minhas").with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/atendimento/cliente/minhas").with(tokenDoCliente(1L)))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoPodeAcessarEndpointsDeFuncionario() throws Exception {
        mockMvc.perform(get("/atendimento/abertos").with(tokenDoCliente(1L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/atendimento/{id}/finalizar", ORDEM_ID).with(tokenDoCliente(1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void relatorioTempoMedioServicosEhRestritoAoAdmin() throws Exception {
        when(atendimentoUseCase.listarTempoMedioExecucaoServicos()).thenReturn(List.of());

        mockMvc.perform(get("/atendimento/relatorios/tempo-medio-servicos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/atendimento/relatorios/tempo-medio-servicos")
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/atendimento/relatorios/tempo-medio-servicos")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void atendenteNaoPodeIniciarDiagnostico() throws Exception {
        mockMvc.perform(patch("/atendimento/{id}/diagnostico/iniciar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void mecanicoPodeIniciarDiagnostico() throws Exception {
        when(atendimentoUseCase.iniciarDiagnostico(ORDEM_ID)).thenReturn(ordem(Status.EM_DIAGNOSTICO));

        mockMvc.perform(patch("/atendimento/{id}/diagnostico/iniciar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoPodePostarDiagnostico() throws Exception {
        when(atendimentoUseCase.realizarDiagnostico(eq(ORDEM_ID), any(), any(), eq("Obs")))
                .thenReturn(ordem(Status.AGUARDANDO_APROVACAO));

        mockMvc.perform(post("/atendimento/{id}/diagnostico", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "observacoes": "Obs"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoNaoPodeAprovarOrdemServico() throws Exception {
        mockMvc.perform(post("/atendimento/{id}/aprovar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendentePodeAprovarOrdemServico() throws Exception {
        when(atendimentoUseCase.aprovarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.EM_EXECUCAO));

        mockMvc.perform(post("/atendimento/{id}/aprovar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoNaoPodeCancelarOrdemServico() throws Exception {
        mockMvc.perform(post("/atendimento/{id}/cancelar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendentePodeCancelarOrdemServico() throws Exception {
        when(atendimentoUseCase.cancelarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.CANCELADA));

        mockMvc.perform(post("/atendimento/{id}/cancelar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoPodeFinalizarOrdemServico() throws Exception {
        when(atendimentoUseCase.finalizarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.FINALIZADA));

        mockMvc.perform(post("/atendimento/{id}/finalizar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isOk());
    }

    @Test
    void atendenteNaoPodeFinalizarOrdemServico() throws Exception {
        mockMvc.perform(post("/atendimento/{id}/finalizar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendentePodeEntregarOrdemServico() throws Exception {
        when(atendimentoUseCase.entregarVeiculo(ORDEM_ID)).thenReturn(ordem(Status.ENTREGUE));

        mockMvc.perform(post("/atendimento/{id}/entregar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void templatesSaoRestritosAoAdmin() throws Exception {
        when(templateUseCase.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/templates")
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/templates")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    private OrdemServico ordemDoCliente(Long clienteId) {
        return OrdemServico.builder()
                .id(ORDEM_ID)
                .clienteId(clienteId)
                .status(Status.RECEBIDA)
                .build();
    }

    private RequestPostProcessor tokenDoCliente(Long clienteId) {
        return jwt().jwt(token -> token.claim("cliente_id", clienteId)).authorities(() -> "ROLE_CLIENTE");
    }

    private OrdemServico ordem(Status status) {
        return OrdemServico.builder()
                .id(ORDEM_ID)
                .status(status)
                .build();
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        AtendimentoUseCase atendimentoUseCase() {
            return Mockito.mock(AtendimentoUseCase.class);
        }

        @Bean
        TemplateUseCase templateUseCase() {
            return Mockito.mock(TemplateUseCase.class);
        }
    }
}
