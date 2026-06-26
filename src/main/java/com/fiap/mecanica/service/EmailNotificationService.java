package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.TemplateNotificacao;
import com.fiap.mecanica.exception.TemplateNotFound;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import com.fiap.mecanica.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private final TemplateRepository templateRepository;
    private final JavaMailSender mailSender;

    @Value("${notificacao.email-atualizacoes}")
    private String emailAtualizacoes;

    @Value("${notificacao.email-remetente}")
    private String emailRemetente;

    public EmailNotificationService(TemplateRepository templateRepository,
                                    JavaMailSender mailSender) {
        this.templateRepository = templateRepository;
        this.mailSender = mailSender;
    }

    @Override
    public void notificarCliente(CodigoTemplate template, Cliente cliente, String... args) {
        TemplateNotificacao templateDomain = buscarTemplate(template);
        String conteudo = formatarConteudo(templateDomain.getConteudo(), args);

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(emailRemetente);
        mensagem.setTo(cliente.getEmail());
        mensagem.setSubject(template.name());
        mensagem.setText(conteudo);

        mailSender.send(mensagem);
    }

    @Override
    public void notificarFuncionarios(CodigoTemplate template, String... args) {
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
