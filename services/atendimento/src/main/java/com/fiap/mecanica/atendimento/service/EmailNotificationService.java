package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.domain.TemplateNotificacao;
import com.fiap.mecanica.atendimento.exception.TemplateNotFound;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private final TemplateRepository templateRepository;
    private final JavaMailSender mailSender;
    private final String emailAtualizacoes;
    private final String emailRemetente;

    public EmailNotificationService(TemplateRepository templateRepository,
                                    JavaMailSender mailSender,
                                    @Value("${notificacao.email-atualizacoes}") String emailAtualizacoes,
                                    @Value("${notificacao.email-remetente}") String emailRemetente) {
        this.templateRepository = templateRepository;
        this.mailSender = mailSender;
        this.emailAtualizacoes = emailAtualizacoes;
        this.emailRemetente = emailRemetente;
    }

    @Override
    public void notificarCliente(CodigoTemplate template, ClienteIntegracaoDto cliente, String... args) {
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
        return templateRepository.findByCodigo(template.name())
                .orElseThrow(() -> new TemplateNotFound(template));
    }

    private String formatarConteudo(String conteudo, String... args) {
        if (args == null || args.length == 0) {
            return conteudo;
        }
        return String.format(conteudo, (Object[]) args);
    }
}
