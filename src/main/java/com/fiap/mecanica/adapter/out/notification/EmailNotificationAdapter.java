package com.fiap.mecanica.adapter.out.notification;

import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.application.exception.TemplateNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationAdapter implements NotificacaoGateway {

    private final TemplateGateway templateGateway;
    private final JavaMailSender mailSender;
    private final String emailAtualizacoes;
    private final String emailRemetente;

    public EmailNotificationAdapter(
            TemplateGateway templateGateway,
            JavaMailSender mailSender,
            @Value("${notificacao.email-atualizacoes}") String emailAtualizacoes,
            @Value("${notificacao.email-remetente}") String emailRemetente) {
        this.templateGateway = templateGateway;
        this.mailSender = mailSender;
        this.emailAtualizacoes = emailAtualizacoes;
        this.emailRemetente = emailRemetente;
    }

    @Override
    public void notificarCliente(CodigoTemplate template, Cliente cliente, String... argumentos) {
        if (emailRemetente.equals("skip")) {
            return;
        }

        enviar(template, cliente.getEmail(), argumentos);
    }

    @Override
    public void notificarFuncionarios(CodigoTemplate template, String... argumentos) {
        if (emailRemetente.equals("skip")) {
            return;
        }

        enviar(template, emailAtualizacoes, argumentos);
    }

    private void enviar(CodigoTemplate codigo, String destinatario, String... argumentos) {
        TemplateNotificacao template = templateGateway.buscarPorCodigo(codigo)
                .orElseThrow(() -> new TemplateNotFound(codigo));

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(emailRemetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject(codigo.name());
        mensagem.setText(formatarConteudo(template.getConteudo(), argumentos));

        mailSender.send(mensagem);
    }

    private String formatarConteudo(String conteudo, String... argumentos) {
        if (argumentos == null || argumentos.length == 0) {
            return conteudo;
        }
        return String.format(conteudo, (Object[]) argumentos);
    }
}
