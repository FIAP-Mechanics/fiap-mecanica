package com.fiap.mecanica.cliente.infrastructure.configuration;

import com.fiap.mecanica.cliente.adapter.in.web.controller.ClienteController;
import com.fiap.mecanica.cliente.application.port.in.ClienteUseCase;
import com.fiap.mecanica.cliente.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.cliente.domain.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteController.class)
@Import({SecurityConfiguration.class, SecurityEndpointAuthorizationTest.MockConfig.class})
@TestPropertySource(properties = "security.jwt.secret=0123456789abcdef0123456789abcdef")
class SecurityEndpointAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteUseCase clienteUseCase;

    @Autowired
    private VinculoVeiculoUseCase vinculoVeiculoUseCase;

    @BeforeEach
    void setUp() {
        reset(clienteUseCase, vinculoVeiculoUseCase);
        Cliente cliente = Cliente.builder().id(1L).nome("Cliente QA").documento("52998224725").build();
        when(clienteUseCase.buscarClientes()).thenReturn(List.of(cliente));
        when(clienteUseCase.buscarClientePorId(1L)).thenReturn(cliente);
        when(clienteUseCase.buscarClientePorDocumento("52998224725")).thenReturn(cliente);
        when(vinculoVeiculoUseCase.listarVeiculosDoCliente(1L)).thenReturn(List.of());
    }

    @Test
    void dadosDeClientesExigemTokenEmTodasAsConsultas() throws Exception {
        for (String path : new String[]{"/cliente", "/cliente/1", "/cliente/documento?documento=52998224725", "/cliente/1/veiculos"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void tokenDeClienteNaoPermiteEnumerarOuConsultarDadosPessoais() throws Exception {
        for (String path : new String[]{"/cliente", "/cliente/1", "/cliente/documento?documento=52998224725", "/cliente/1/veiculos"}) {
            mockMvc.perform(get(path).with(jwt().authorities(() -> "ROLE_CLIENTE")))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void funcionariosAutorizadosPodemConsultarClientes() throws Exception {
        for (String path : new String[]{"/cliente", "/cliente/1", "/cliente/documento?documento=52998224725", "/cliente/1/veiculos"}) {
            mockMvc.perform(get(path).with(jwt().authorities(() -> "ROLE_ATENDENTE")))
                    .andExpect(status().isOk());
            mockMvc.perform(get(path).with(jwt().authorities(() -> "ROLE_ADMIN")))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void mecanicoNaoPodeConsultarDadosPessoais() throws Exception {
        mockMvc.perform(get("/cliente").with(jwt().authorities(() -> "ROLE_MECANICO")))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        ClienteUseCase clienteUseCase() {
            return Mockito.mock(ClienteUseCase.class);
        }

        @Bean
        VinculoVeiculoUseCase vinculoVeiculoUseCase() {
            return Mockito.mock(VinculoVeiculoUseCase.class);
        }
    }
}
