package com.fiap.mecanica.atendimento.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Status {
    RECEBIDA("Recebida", "Seu veículo foi recebido e em breve iniciaremos o diagnóstico!"),
    EM_DIAGNOSTICO("Em diagnóstico", "Estamos avaliando seu veículo e entraremos em contato em breve!"),
    AGUARDANDO_APROVACAO("Aguardando aprovação", "Seu orçamento está pronto! Aguardamos sua aprovação para iniciar o serviço."),
    EM_EXECUCAO("Em execução", "Ótimas notícias! Nossa equipe já está trabalhando no seu veículo."),
    FINALIZADA("Finalizada", "Tudo pronto! Seu veículo está finalizado e aguardando sua retirada."),
    CANCELADA("Cancelada", "Ordem de serviço cancelada após recusa do orçamento."),
    ENTREGUE("Entregue", "Serviço concluído com sucesso! Obrigado por confiar em nossos serviços.");

    private final String nome;
    private final String descricao;
}
