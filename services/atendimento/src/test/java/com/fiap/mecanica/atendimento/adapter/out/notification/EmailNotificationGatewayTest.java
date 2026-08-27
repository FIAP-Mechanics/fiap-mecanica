package com.fiap.mecanica.atendimento.adapter.out.notification;

import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationGatewayTest {

    private static final String EMAIL_REMETENTE = "noreply@oficina.com";
    private static final String EMAIL_ADMIN = "admin@oficina.com";
    private static final String EMAIL_CLIENTE = "cliente@teste.com";
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

    // ===================== notificarCliente =====================

    @Test
    void deveEnviarEmailParaClienteComSucesso() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoGateway.ClienteIntegracao cliente = criarCliente();
        TemplateNotificacao template = criarTemplate(codigo);

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.of(template));

        gateway.notificarCliente(codigo, cliente, NOME_CLIENTE, PLACA_VEICULO);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(EMAIL_CLIENTE);
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).isEqualTo(String.format(CONTEUDO_TEMPLATE, NOME_CLIENTE, PLACA_VEICULO));
    }

    @Test
    void deveEnviarEmailParaClienteComSucessoSemArgs() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoGateway.ClienteIntegracao cliente = criarCliente();
        TemplateNotificacao template = TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo("Conteudo sem args")
                .build();

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.of(template));

        gateway.notificarCliente(codigo, cliente);

        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Conteudo sem args");
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarCliente() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoGateway.ClienteIntegracao cliente = criarCliente();

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateway.notificarCliente(codigo, cliente))
                .isInstanceOf(TemplateNotFound.class)
                .hasMessageContaining(codigo.name());

        verifyNoInteractions(mailSender);
    }

    @Test
    void deveIgnorarEnvioQuandoEmailRemetenteForSkipAoNotificarCliente() {
        EmailNotificationGateway gateway = criarGateway("skip", EMAIL_ADMIN);
        ClienteIntegracaoGateway.ClienteIntegracao cliente = criarCliente();

        gateway.notificarCliente(CodigoTemplate.RETIRAR_VEICULO, cliente);

        verifyNoInteractions(templateGateway, mailSender);
    }

    // ===================== notificarFuncionarios =====================

    @Test
    void deveEnviarEmailParaFuncionariosComSucesso() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;
        TemplateNotificacao template = criarTemplateSimples(codigo);
        String arg1 = "Insumo X";

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.of(template));

        gateway.notificarFuncionarios(codigo, arg1);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(EMAIL_ADMIN);
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).contains(arg1);
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarFuncionarios() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gateway.notificarFuncionarios(codigo))
                .isInstanceOf(TemplateNotFound.class);

        verifyNoInteractions(mailSender);
    }

    @Test
    void deveIgnorarEnvioQuandoEmailRemetenteForSkipAoNotificarFuncionarios() {
        EmailNotificationGateway gateway = criarGateway("skip", EMAIL_ADMIN);

        gateway.notificarFuncionarios(CodigoTemplate.REPOSICAO_ESTOQUE, "arg");

        verifyNoInteractions(templateGateway, mailSender);
    }

    @Test
    void naoDevePropagarFalhaDoServidorSmtp() {
        EmailNotificationGateway gateway = criarGateway(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.REPOSICAO_ESTOQUE;
        TemplateNotificacao template = criarTemplateSimples(codigo);

        when(templateGateway.buscarPorCodigo(codigo.name())).thenReturn(Optional.of(template));
        doThrow(new MailSendException("Limite de envio excedido"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> gateway.notificarFuncionarios(codigo, "Insumo X"))
                .doesNotThrowAnyException();

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    private EmailNotificationGateway criarGateway(String emailRemetente, String emailAtualizacoes) {
        return new EmailNotificationGateway(templateGateway, mailSender, emailAtualizacoes, emailRemetente);
    }

    private ClienteIntegracaoGateway.ClienteIntegracao criarCliente() {
        return new ClienteIntegracaoGateway.ClienteIntegracao(1L, "Cliente Teste", "12345678901", EMAIL_CLIENTE);
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
