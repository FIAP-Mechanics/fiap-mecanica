package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.TemplateRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    private static final String EMAIL_REMETENTE = "noreply@oficina.com";
    private static final String EMAIL_ADMIN = "admin@oficina.com";
    private static final String EMAIL_CLIENTE = "cliente@teste.com";
    private static final String CONTEUDO_TEMPLATE = "Ola %s, seu veiculo %s esta pronto.";
    private static final String CONTEUDO_TEMPLATE_SIMPLES = "Ola, houve uma atualizacao: %s";
    private static final String NOME_CLIENTE = "Joao";
    private static final String PLACA_VEICULO = "ABC-1234";

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    // ===================== notificarCliente =====================

    @Test
    void deveEnviarEmailParaClienteComSucesso() {
        EmailNotificationService service = criarService(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoDto cliente = criarCliente();
        TemplateNotificacao template = criarTemplate(codigo);

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        service.notificarCliente(codigo, cliente, NOME_CLIENTE, PLACA_VEICULO);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(EMAIL_CLIENTE);
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).isEqualTo(String.format(CONTEUDO_TEMPLATE, NOME_CLIENTE, PLACA_VEICULO));
    }

    @Test
    void deveEnviarEmailParaClienteComSucessoSemArgs() {
        EmailNotificationService service = criarService(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoDto cliente = criarCliente();
        TemplateNotificacao template = TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo("Conteudo sem args")
                .build();

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        service.notificarCliente(codigo, cliente);

        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Conteudo sem args");
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarCliente() {
        EmailNotificationService service = criarService(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        ClienteIntegracaoDto cliente = criarCliente();

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.notificarCliente(codigo, cliente))
                .isInstanceOf(TemplateNotFound.class)
                .hasMessageContaining(codigo.name());

        verifyNoInteractions(mailSender);
    }

    @Test
    void deveIgnorarEnvioQuandoEmailRemetenteForSkipAoNotificarCliente() {
        EmailNotificationService service = criarService("skip", EMAIL_ADMIN);
        ClienteIntegracaoDto cliente = criarCliente();

        service.notificarCliente(CodigoTemplate.RETIRAR_VEICULO, cliente);

        verifyNoInteractions(templateRepository, mailSender);
    }

    // ===================== notificarFuncionarios =====================

    @Test
    void deveEnviarEmailParaFuncionariosComSucesso() {
        EmailNotificationService service = criarService(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;
        TemplateNotificacao template = criarTemplateSimples(codigo);
        String arg1 = "Insumo X";

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        service.notificarFuncionarios(codigo, arg1);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(EMAIL_ADMIN);
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).contains(arg1);
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarFuncionarios() {
        EmailNotificationService service = criarService(EMAIL_REMETENTE, EMAIL_ADMIN);
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.notificarFuncionarios(codigo))
                .isInstanceOf(TemplateNotFound.class);

        verifyNoInteractions(mailSender);
    }

    @Test
    void deveIgnorarEnvioQuandoEmailRemetenteForSkipAoNotificarFuncionarios() {
        EmailNotificationService service = criarService("skip", EMAIL_ADMIN);

        service.notificarFuncionarios(CodigoTemplate.REPOSICAO_ESTOQUE, "arg");

        verifyNoInteractions(templateRepository, mailSender);
    }

    private EmailNotificationService criarService(String emailRemetente, String emailAtualizacoes) {
        return new EmailNotificationService(templateRepository, mailSender, emailAtualizacoes, emailRemetente);
    }

    private ClienteIntegracaoDto criarCliente() {
        return new ClienteIntegracaoDto(1L, "Cliente Teste", "12345678901", EMAIL_CLIENTE);
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
