package com.fiap.mecanica.servico.application.usecase;

import com.fiap.mecanica.servico.application.command.AtualizarServicoCommand;
import com.fiap.mecanica.servico.application.port.in.ServicoUseCase;
import com.fiap.mecanica.servico.application.port.out.ServicoGateway;
import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.exception.ServicoInativoException;
import com.fiap.mecanica.servico.exception.ServicoJaAtivoException;
import com.fiap.mecanica.servico.exception.ServicoNotFound;

import java.util.List;
import java.util.function.Consumer;

public class ServicoInteractor implements ServicoUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoInteractor(ServicoGateway servicoGateway) {
        this.servicoGateway = servicoGateway;
    }

    @Override
    public List<Servico> buscarTodos() {
        return servicoGateway.buscarTodos();
    }

    @Override
    public Servico cadastrarServico(Servico servico) {
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico buscarServicoPorId(Long idServico) {
        Servico servico = servicoGateway.buscarPorId(idServico).orElseThrow(() -> new ServicoNotFound(idServico));
        if (!servico.isAtivo()) throw new ServicoInativoException(idServico);
        return servico;
    }

    @Override
    public Servico atualizarServico(Long idServico, AtualizarServicoCommand command) {
        Servico servico = this.buscarServicoPorId(idServico);

        atualizaSeExistente(command.nome(), servico::setNome);
        atualizaSeExistente(command.descricao(), servico::setDescricao);
        atualizaSeExistente(command.valor(), servico::setValor);
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico excluirServico(Long idServico) {
        Servico servico = this.buscarServicoPorId(idServico);
        servico.setAtivo(false);
        return servicoGateway.salvar(servico);
    }

    @Override
    public Servico reativarServico(Long idServico) {
        Servico servico = servicoGateway.buscarPorId(idServico).orElseThrow(() -> new ServicoNotFound(idServico));
        if (servico.isAtivo()) throw new ServicoJaAtivoException(idServico);
        servico.setAtivo(true);
        return servicoGateway.salvar(servico);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
