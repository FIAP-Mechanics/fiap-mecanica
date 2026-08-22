package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.application.command.ServicoTempoCommand;
import com.fiap.mecanica.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.application.port.in.InsumoUseCase;
import com.fiap.mecanica.application.port.in.ServicoUseCase;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.application.result.TempoMedioExecucaoServicoResult;
import com.fiap.mecanica.domain.Cliente;
import com.fiap.mecanica.domain.CodigoTemplate;
import com.fiap.mecanica.domain.Insumo;
import com.fiap.mecanica.domain.Orcamento;
import com.fiap.mecanica.domain.OrdemServico;
import com.fiap.mecanica.domain.OrdemServicoServico;
import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.domain.Status;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.exception.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtendimentoInteractor implements AtendimentoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final ClienteUseCase clienteUseCase;
    private final VeiculoUseCase veiculoUseCase;
    private final ServicoUseCase servicoUseCase;
    private final InsumoUseCase insumoUseCase;
    private final EstoqueUseCase estoqueUseCase;
    private final NotificacaoGateway notificacaoGateway;

    public AtendimentoInteractor(
            OrdemServicoGateway ordemServicoGateway,
            ClienteUseCase clienteUseCase,
            VeiculoUseCase veiculoUseCase,
            ServicoUseCase servicoUseCase,
            InsumoUseCase insumoUseCase,
            EstoqueUseCase estoqueUseCase,
            NotificacaoGateway notificacaoGateway) {
        this.ordemServicoGateway = ordemServicoGateway;
        this.clienteUseCase = clienteUseCase;
        this.veiculoUseCase = veiculoUseCase;
        this.servicoUseCase = servicoUseCase;
        this.insumoUseCase = insumoUseCase;
        this.estoqueUseCase = estoqueUseCase;
        this.notificacaoGateway = notificacaoGateway;
    }

    @Override
    public OrdemServico iniciarAtendimento(
            Long clienteId,
            Long veiculoId,
            String relatoCliente,
            List<ServicoQuantidadeCommand> servicos,
            List<InsumoQuantidadeCommand> insumos) {
        Cliente cliente = clienteUseCase.buscarClientePorId(clienteId);
        Veiculo veiculo = veiculoUseCase.buscarVeiculoPorId(veiculoId);

        OrdemServico ordemServico = OrdemServico.builder()
                .cliente(cliente)
                .veiculo(veiculo)
                .relatoCliente(relatoCliente)
                .build();
        ordemServico.setStatus(Status.RECEBIDA);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .build();
        adicionarItensAoOrcamento(servicos, insumos, orcamento);

        ordemServico.setOrcamento(orcamento);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico buscarPorId(String id) {
        return buscarOrdemServico(id);
    }

    @Override
    public List<OrdemServico> listarAtendimentosEmAberto() {
        return ordemServicoGateway.buscarPorStatusForaDe(
                List.of(Status.ENTREGUE, Status.CANCELADA));
    }

    @Override
    public List<TempoMedioExecucaoServicoResult> listarTempoMedioExecucaoServicos() {
        Map<Long, TempoMedioServico> indicadores = new HashMap<>();
        List<OrdemServico> ordensConcluidas = ordemServicoGateway.buscarPorStatusEm(
                List.of(Status.FINALIZADA, Status.ENTREGUE));

        for (OrdemServico ordemServico : ordensConcluidas) {
            if (ordemServico.getOrcamento() == null
                    || ordemServico.getOrcamento().getServicos() == null) {
                continue;
            }

            for (OrdemServicoServico ordemServicoServico
                    : ordemServico.getOrcamento().getServicos()) {
                Servico servico = ordemServicoServico.getServico();
                if (servico == null
                        || servico.getId() == null
                        || ordemServicoServico.getTempoExecucaoMinutos() == null) {
                    continue;
                }

                TempoMedioServico indicador = indicadores.computeIfAbsent(
                        servico.getId(),
                        id -> new TempoMedioServico(servico.getId(), servico.getNome()));
                indicador.adicionar(ordemServicoServico.getTempoExecucaoMinutos());
            }
        }

        return indicadores.values().stream()
                .map(TempoMedioServico::toResult)
                .sorted(Comparator.comparing(
                        TempoMedioExecucaoServicoResult::nome,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public OrdemServico iniciarDiagnostico(String id) {
        OrdemServico ordemServico = buscarOrdemServico(id);
        ordemServico.atualizarStatus(Status.EM_DIAGNOSTICO);
        ordemServicoGateway.salvar(ordemServico);
        return ordemServico;
    }

    @Override
    public OrdemServico realizarDiagnostico(
            String id,
            List<ServicoQuantidadeCommand> servicos,
            List<InsumoQuantidadeCommand> insumos,
            String observacoesDiagnostico) {
        OrdemServico ordemServico = buscarOrdemServico(id);

        if (ordemServico.getStatus() != Status.EM_DIAGNOSTICO) {
            throw new ValidacaoException(
                    "Apenas ordens em diagnóstico podem receber novos itens.");
        }

        if (observacoesDiagnostico != null) {
            ordemServico.setObservacoesDiagnostico(observacoesDiagnostico);
        }

        adicionarItensAoOrcamento(
                servicos,
                insumos,
                ordemServico.getOrcamento());

        ordemServico.atualizarStatus(Status.AGUARDANDO_APROVACAO);
        notificacaoGateway.notificarCliente(
                CodigoTemplate.AUTORIZAR_ORCAMENTO,
                ordemServico.getCliente());
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico aprovarOrdemServico(String id) {
        OrdemServico ordemServico = buscarOrdemServico(id);

        if (ordemServico.getOrcamento() != null
                && ordemServico.getOrcamento().getInsumos() != null) {
            estoqueUseCase.deduzirEstoque(ordemServico.getOrcamento().getInsumos());
        }

        ordemServico.atualizarStatus(Status.EM_EXECUCAO);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico cancelarOrdemServico(String id) {
        OrdemServico ordemServico = buscarOrdemServico(id);
        ordemServico.atualizarStatus(Status.CANCELADA);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico finalizarOrdemServico(
            String id,
            List<ServicoTempoCommand> servicosTempo) {
        OrdemServico ordemServico = buscarOrdemServico(id);

        registrarTempoExecucaoServicos(ordemServico, servicosTempo);
        ordemServico.atualizarStatus(Status.FINALIZADA);
        notificacaoGateway.notificarCliente(
                CodigoTemplate.RETIRAR_VEICULO,
                ordemServico.getCliente());
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico entregarVeiculo(String id) {
        OrdemServico ordemServico = buscarOrdemServico(id);
        ordemServico.atualizarStatus(Status.ENTREGUE);
        notificacaoGateway.notificarCliente(
                CodigoTemplate.VEICULO_RETIRADO,
                ordemServico.getCliente());
        return ordemServicoGateway.salvar(ordemServico);
    }

    private void adicionarItensAoOrcamento(
            List<ServicoQuantidadeCommand> servicos,
            List<InsumoQuantidadeCommand> insumos,
            Orcamento orcamento) {
        if (servicos != null) {
            servicos.forEach(item -> {
                Servico servico = servicoUseCase.buscarServicoPorId(item.servico());
                orcamento.adicionarServico(servico, item.quantidade());
            });
        }

        if (insumos != null) {
            insumos.forEach(item -> {
                Insumo insumo = insumoUseCase.buscarInsumoPorId(item.insumo());
                orcamento.adicionarInsumo(insumo, item.quantidade());
            });
        }
    }

    private void registrarTempoExecucaoServicos(
            OrdemServico ordemServico,
            List<ServicoTempoCommand> servicosTempo) {
        if (servicosTempo == null || servicosTempo.isEmpty()) {
            throw new ValidacaoException(
                    "Informe o tempo gasto nos servicos da ordem de servico.");
        }

        if (ordemServico.getOrcamento() == null
                || ordemServico.getOrcamento().getServicos() == null
                || ordemServico.getOrcamento().getServicos().isEmpty()) {
            throw new ValidacaoException(
                    "A ordem de servico nao possui servicos para registrar tempo.");
        }

        for (ServicoTempoCommand servicoTempo : servicosTempo) {
            OrdemServicoServico servicoDaOrdem =
                    ordemServico.getOrcamento().getServicos().stream()
                            .filter(item -> item.getServico() != null
                                    && item.getServico().getId().equals(servicoTempo.servico()))
                            .findFirst()
                            .orElseThrow(() -> new ValidacaoException(
                                    "Servico " + servicoTempo.servico()
                                            + " nao pertence a ordem de servico."));

            servicoDaOrdem.setTempoExecucaoMinutos(servicoTempo.tempoGastoMinutos());
        }

        boolean existeServicoSemTempo =
                ordemServico.getOrcamento().getServicos().stream()
                        .anyMatch(item -> item.getTempoExecucaoMinutos() == null);

        if (existeServicoSemTempo) {
            throw new ValidacaoException(
                    "Informe o tempo gasto de todos os servicos da ordem de servico.");
        }
    }

    private OrdemServico buscarOrdemServico(String id) {
        return ordemServicoGateway.buscarPorId(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
    }

    private static final class TempoMedioServico {
        private final Long servicoId;
        private final String nome;
        private long ordensFinalizadas;
        private long tempoTotalMinutos;

        private TempoMedioServico(Long servicoId, String nome) {
            this.servicoId = servicoId;
            this.nome = nome;
        }

        private void adicionar(Long tempoExecucaoMinutos) {
            ordensFinalizadas++;
            tempoTotalMinutos += tempoExecucaoMinutos;
        }

        private TempoMedioExecucaoServicoResult toResult() {
            BigDecimal tempoMedioMinutos = BigDecimal.valueOf(tempoTotalMinutos)
                    .divide(
                            BigDecimal.valueOf(ordensFinalizadas),
                            2,
                            RoundingMode.HALF_UP);
            return new TempoMedioExecucaoServicoResult(
                    servicoId,
                    nome,
                    ordensFinalizadas,
                    tempoMedioMinutos);
        }
    }
}
