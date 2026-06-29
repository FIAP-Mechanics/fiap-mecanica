package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.exception.TemplateNotFound;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import com.fiap.mecanica.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    private static final String EMAIL_REMETENTE = "noreply@oficina.com";
    private static final String EMAIL_ADMIN = "admin@oficina.com";
    private static final String CONTEUDO_TEMPLATE = "Ola %s, seu veiculo %s esta pronto.";
    private static final String CONTEUDO_TEMPLATE_SIMPLES = "Ola, houve uma atualizacao: %s";
    private static final String NOME_CLIENTE = "Joao";
    private static final String PLACA_VEICULO = "ABC-1234";

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    @BeforeEach
    void configurar() {
        ReflectionTestUtils.setField(emailNotificationService, "emailRemetente", EMAIL_REMETENTE);
        ReflectionTestUtils.setField(emailNotificationService, "emailAtualizacoes", EMAIL_ADMIN);
    }

    // ===================== notificarCliente =====================

    @Test
    void deveEnviarEmailParaClienteComSucesso() {
        // Arrange
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        Cliente cliente = criarCliente();
        TemplateNotificacao template = criarTemplate(codigo);

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        // Act
        emailNotificationService.notificarCliente(codigo, cliente, NOME_CLIENTE, PLACA_VEICULO);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(cliente.getEmail());
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).isEqualTo(String.format(CONTEUDO_TEMPLATE, NOME_CLIENTE, PLACA_VEICULO));
    }

    @Test
    void deveEnviarEmailParaClienteComSucessoSemArgs() {
        // Arrange
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        Cliente cliente = criarCliente();
        TemplateNotificacao template = TemplateNotificacao.builder()
                .codigo(codigo.name())
                .conteudo("Conteudo sem args")
                .build();

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        // Act
        emailNotificationService.notificarCliente(codigo, cliente);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Conteudo sem args");
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarCliente() {
        // Arrange
        CodigoTemplate codigo = CodigoTemplate.RETIRAR_VEICULO;
        Cliente cliente = criarCliente();

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> emailNotificationService.notificarCliente(codigo, cliente))
                .isInstanceOf(TemplateNotFound.class)
                .hasMessageContaining(codigo.name());

        verifyNoInteractions(mailSender);
    }

    // ===================== notificarFuncionarios =====================

    @Test
    void deveEnviarEmailParaFuncionariosComSucesso() {
        // Arrange
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;
        TemplateNotificacao template = criarTemplateSimples(codigo);
        String arg1 = "Insumo X";

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.of(template));

        // Act
        emailNotificationService.notificarFuncionarios(codigo, arg1);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage mensagemEnviada = messageCaptor.getValue();

        assertThat(mensagemEnviada.getFrom()).isEqualTo(EMAIL_REMETENTE);
        assertThat(mensagemEnviada.getTo()).containsExactly(EMAIL_ADMIN);
        assertThat(mensagemEnviada.getSubject()).isEqualTo(codigo.name());
        assertThat(mensagemEnviada.getText()).contains(arg1);
    }

    @Test
    void deveLancarExcecaoQuandoTemplateNaoEncontradoAoNotificarFuncionarios() {
        // Arrange
        CodigoTemplate codigo = CodigoTemplate.VEICULO_RETIRADO;

        when(templateRepository.findByCodigo(codigo.name())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> emailNotificationService.notificarFuncionarios(codigo))
                .isInstanceOf(TemplateNotFound.class);

        verifyNoInteractions(mailSender);
    }

    // Métodos auxiliares privados

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
