package com.fiap.mecanica.config;

import com.fiap.mecanica.controller.AtendimentoController;
import com.fiap.mecanica.controller.TemplateController;
import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.service.OrdemServicoService;
import com.fiap.mecanica.service.TemplateService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AtendimentoController.class, TemplateController.class})
@Import({SecurityConfig.class, SecurityEndpointAuthorizationTest.MockConfig.class})
@TestPropertySource(properties = "security.jwt.secret=0123456789abcdef0123456789abcdef")
class SecurityEndpointAuthorizationTest {

    private static final String ORDEM_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrdemServicoService ordemServicoService;
    @Autowired
    private TemplateService templateService;

    @BeforeEach
    void setUp() {
        reset(ordemServicoService, templateService);
    }

    @Test
    void atendentePodeIniciarAtendimento() throws Exception {
        when(ordemServicoService.iniciarAtendimento(eq(1L), eq(2L), eq("Nao liga"), any(), any()))
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
    void clientePodeAcompanharOrdemServicoSemAutenticacao() throws Exception {
        when(ordemServicoService.buscarPorId(ORDEM_ID)).thenReturn(ordem(Status.EM_DIAGNOSTICO));

        mockMvc.perform(get("/atendimento/{id}", ORDEM_ID))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoPodeListarAtendimentosAbertosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/atendimento/abertos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void atendenteNaoPodeIniciarDiagnostico() throws Exception {
        mockMvc.perform(patch("/atendimento/{id}/diagnostico/iniciar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void mecanicoPodeIniciarDiagnostico() throws Exception {
        when(ordemServicoService.iniciarDiagnostico(ORDEM_ID)).thenReturn(ordem(Status.EM_DIAGNOSTICO));

        mockMvc.perform(patch("/atendimento/{id}/diagnostico/iniciar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoPodePostarDiagnostico() throws Exception {
        when(ordemServicoService.realizarDiagnostico(eq(ORDEM_ID), any(), any(), eq("Obs")))
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
        when(ordemServicoService.aprovarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.EM_EXECUCAO));

        mockMvc.perform(post("/atendimento/{id}/aprovar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void mecanicoPodeFinalizarOrdemServico() throws Exception {
        when(ordemServicoService.finalizarOrdemServico(ORDEM_ID)).thenReturn(ordem(Status.FINALIZADA));

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
        when(ordemServicoService.entregarVeiculo(ORDEM_ID)).thenReturn(ordem(Status.ENTREGUE));

        mockMvc.perform(post("/atendimento/{id}/entregar", ORDEM_ID)
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void templatesSaoRestritosAoAdmin() throws Exception {
        when(templateService.buscarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/templates")
                        .with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/templates")
                        .with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    private OrdemServicoDto ordem(Status status) {
        return OrdemServicoDto.builder()
                .id(ORDEM_ID)
                .status(status)
                .build();
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        OrdemServicoService ordemServicoService() {
            return Mockito.mock(OrdemServicoService.class);
        }

        @Bean
        TemplateService templateService() {
            return Mockito.mock(TemplateService.class);
        }
    }
}
