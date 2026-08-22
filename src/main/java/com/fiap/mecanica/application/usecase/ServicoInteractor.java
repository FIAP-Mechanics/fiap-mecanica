package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.application.port.in.ServicoUseCase;
import com.fiap.mecanica.application.port.out.ServicoGateway;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.exception.ServicoInativoException;
import com.fiap.mecanica.exception.ServicoJaAtivoException;
import com.fiap.mecanica.exception.ServicoNotFound;

import java.util.function.Consumer;

public class ServicoInteractor implements ServicoUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoInteractor(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    @Override
    public Servico cadastrarServico(Servico servico) {
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico buscarServicoPorId(Long idServico) {
        Servico servico = servicoGateway.buscarPorId(idServico)
                .orElseThrow(() -> new ServicoNotFound(idServico));
        if (!servico.isAtivo()) {
            throw new ServicoInativoException(idServico);
        }
        return servico;
    }

    @Override
    public Servico atualizarServico(Long idServico, AtualizarServicoCommand command) {
        Servico servico = buscarServicoPorId(idServico);
        atualizarSeExistente(command.nome(), servico::setNome);
        atualizarSeExistente(command.descricao(), servico::setDescricao);
        atualizarSeExistente(command.valor(), servico::setValor);
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico excluirServico(Long idServico) {
        Servico servico = buscarServicoPorId(idServico);
        servico.setAtivo(false);
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico reativarServico(Long idServico) {
        Servico servico = servicoGateway.buscarPorId(idServico)
                .orElseThrow(() -> new ServicoNotFound(idServico));
        if (servico.isAtivo()) {
            throw new ServicoJaAtivoException(idServico);
        }
        servico.setAtivo(true);
        return servicoGateway.salvar(servico);
    }

    private <T> void atualizarSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
