package com.fiap.mecanica.atendimento.adapter.out.notification;

import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.NotificationGateway;
import com.fiap.mecanica.atendimento.application.port.out.TemplateGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationGateway implements NotificationGateway {

    private final TemplateGateway templateGateway;
    private final JavaMailSender mailSender;
    private final String emailAtualizacoes;
    private final String emailRemetente;

    public EmailNotificationGateway(TemplateGateway templateGateway,
                                     JavaMailSender mailSender,
                                     @Value("${notificacao.email-atualizacoes}") String emailAtualizacoes,
                                     @Value("${notificacao.email-remetente}") String emailRemetente) {
        this.templateGateway = templateGateway;
        this.mailSender = mailSender;
        this.emailAtualizacoes = emailAtualizacoes;
        this.emailRemetente = emailRemetente;
    }

    @Override
    public void notificarCliente(CodigoTemplate template, ClienteIntegracaoGateway.ClienteIntegracao cliente, String... args) {
        if (this.emailRemetente.equals("skip")) return;

        TemplateNotificacao templateDomain = buscarTemplate(template);
        String conteudo = formatarConteudo(templateDomain.getConteudo(), args);

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(emailRemetente);
        mensagem.setTo(cliente.email());
        mensagem.setSubject(template.name());
        mensagem.setText(conteudo);

        mailSender.send(mensagem);
    }

    @Override
    public void notificarFuncionarios(CodigoTemplate template, String... args) {
        if (this.emailRemetente.equals("skip")) return;

        TemplateNotificacao templateDomain = buscarTemplate(template);
        String conteudo = formatarConteudo(templateDomain.getConteudo(), args);

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(emailRemetente);
        mensagem.setTo(emailAtualizacoes);
        mensagem.setSubject(template.name());
        mensagem.setText(conteudo);

        mailSender.send(mensagem);
    }

    private TemplateNotificacao buscarTemplate(CodigoTemplate template) {
        return templateGateway.buscarPorCodigo(template.name())
                .orElseThrow(() -> new TemplateNotFound(template));
    }

    private String formatarConteudo(String conteudo, String... args) {
        if (args == null || args.length == 0) {
            return conteudo;
        }
        return String.format(conteudo, (Object[]) args);
    }
}
