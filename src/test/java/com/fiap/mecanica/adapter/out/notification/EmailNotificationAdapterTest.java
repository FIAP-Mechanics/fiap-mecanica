package com.fiap.mecanica.adapter.out.notification;

import com.fiap.mecanica.application.exception.TemplateNotFound;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    private static final String EMAIL_REMETENTE = "noreply@oficina.com";
    private static final String EMAIL_ADMIN = "admin@oficina.com";
    private static final String CONTEUDO_TEMPLATE = "Ola %s, seu veiculo %s esta pronto.";
    private static final String CONTEUDO_TEMPLATE_SIMPLES = "Ola, houve uma atualizacao: %s";
    private static final String NOME_CLIENTE = "Joao";
    private static final String PLACA_VEICULO = "ABC-1234";

    @Mock
    private TemplateGateway templateGateway;

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private EmailNotificationAdapter adapter;

    @BeforeEach
    void configurar() {
        adapter = new EmailNotificationAdapter(
                templateGateway,
                mailSender,
                EMAIL_ADMIN,
                EMAIL_REMETENTE);
    }

    @Test
    void deveEnviarEmailParaClienteComSucesso() {
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        Cliente cliente = criarCliente();
        when(templateGateway.buscarPorCodigo(codigo))
                .thenReturn(Optional.of(criarTemplate(codigo)));

        adapter.notificarCliente(codigo, cliente, NOME_CLIENTE, PLACA_VEICULO);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();
        assertThat(mensagem.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagem.getTo()).containsExactly(cliente.getEmail());
        assertThat(mensagem.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagem.getText())
                .isEqualTo(String.format(CONTEUDO_TEMPLATE, NOME_CLIENTE, PLACA_VEICULO));
    }

    @Test
    void deveEnviarEmailParaClienteComSucessoSemArgs() {
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        Cliente cliente = criarCliente();
        TemplateNotificacao template = TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo("Conteudo sem args")
                .build();
        when(templateGateway.buscarPorCodigo(codigo)).thenReturn(Optional.of(template));

        adapter.notificarCliente(codigo, cliente);

        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Conteudo sem args");
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarCliente() {
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        when(templateGateway.buscarPorCodigo(codigo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.notificarCliente(codigo, criarCliente()))
                .isInstanceOf(TemplateNotFound.class)
                .hasMessageContaining(codigo.name());
        verifyNoInteractions(mailSender);
    }

    @Test
    void deveEnviarEmailParaFuncionariosComSucesso() {
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;
        when(templateGateway.buscarPorCodigo(codigo))
                .thenReturn(Optional.of(criarTemplateSimples(codigo)));

        adapter.notificarFuncionarios(codigo, "Insumo X");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagem = messageCaptor.getValue();
        assertThat(mensagem.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagem.getTo()).containsExactly(EMAIL_ADMIN);
        assertThat(mensagem.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagem.getText()).contains("Insumo X");
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarFuncionarios() {
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;
        when(templateGateway.buscarPorCodigo(codigo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.notificarFuncionarios(codigo))
                .isInstanceOf(TemplateNotFound.class);
        verifyNoInteractions(mailSender);
    }

    private Cliente criarCliente() {
        return Cliente.builder()
                .nome("Cliente Teste")
                .email("cliente@teste.com")
                .documento("12345678901")
                .build();
    }

    private TemplateNotificacao criarTemplate(CodigoTemplate codigo) {
        return TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo(CONTEUDO_TEMPLATE)
                .build();
    }

    private TemplateNotificacao criarTemplateSimples(CodigoTemplate codigo) {
        return TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo(CONTEUDO_TEMPLATE_SIMPLES)
                .build();
    }
}
