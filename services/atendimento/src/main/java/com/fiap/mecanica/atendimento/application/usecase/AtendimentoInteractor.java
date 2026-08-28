package com.fiap.mecanica.atendimento.application.usecase;

import com.fiap.mecanica.atendimento.application.command.InsumoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.command.ServicoQuantidadeCommand;
import com.fiap.mecanica.atendimento.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.atendimento.application.port.out.ClienteIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.EstoqueIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.NotificationGateway;
import com.fiap.mecanica.atendimento.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.atendimento.application.port.out.ServicoIntegracaoGateway;
import com.fiap.mecanica.atendimento.application.port.out.VeiculoIntegracaoGateway;
import com.fiap.mecanica.atendimento.domain.CodigoTemplate;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.Orcamento;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.domain.TempoMedioExecucaoServico;
import com.fiap.mecanica.atendimento.domain.TrocaStatus;
import com.fiap.mecanica.atendimento.exception.EstoqueInsuficienteException;
import com.fiap.mecanica.atendimento.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.atendimento.exception.ValidacaoException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtendimentoInteractor implements AtendimentoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final ClienteIntegracaoGateway clienteIntegracaoGateway;
    private final VeiculoIntegracaoGateway veiculoIntegracaoGateway;
    private final ServicoIntegracaoGateway servicoIntegracaoGateway;
    private final EstoqueIntegracaoGateway estoqueIntegracaoGateway;
    private final NotificationGateway notificationGateway;

    private static final Map<Status, Integer> PRIORIDADE_LISTAGEM = Map.of(
            Status.EM_EXECUCAO, 0,
            Status.AGUARDANDO_APROVACAO, 1,
            Status.EM_DIAGNOSTICO, 2,
            Status.RECEBIDA, 3
    );

    public AtendimentoInteractor(OrdemServicoGateway ordemServicoGateway,
                                  ClienteIntegracaoGateway clienteIntegracaoGateway,
                                  VeiculoIntegracaoGateway veiculoIntegracaoGateway,
                                  ServicoIntegracaoGateway servicoIntegracaoGateway,
                                  EstoqueIntegracaoGateway estoqueIntegracaoGateway,
                                  NotificationGateway notificationGateway) {
        this.ordemServicoGateway = ordemServicoGateway;
        this.clienteIntegracaoGateway = clienteIntegracaoGateway;
        this.veiculoIntegracaoGateway = veiculoIntegracaoGateway;
        this.servicoIntegracaoGateway = servicoIntegracaoGateway;
        this.estoqueIntegracaoGateway = estoqueIntegracaoGateway;
        this.notificationGateway = notificationGateway;
    }

    @Override
    public OrdemServico iniciarAtendimento(Long clienteId, Long veiculoId, String relatoCliente,
                                            List<ServicoQuantidadeCommand> servicos, List<InsumoQuantidadeCommand> insumos) {
        clienteIntegracaoGateway.buscarCliente(clienteId);
        veiculoIntegracaoGateway.buscarVeiculo(veiculoId);

        OrdemServico ordemServico = OrdemServico.builder()
                .status(Status.RECEBIDA)
                .clienteId(clienteId)
                .veiculoId(veiculoId)
                .relatoCliente(relatoCliente)
                .dataCriacao(LocalDateTime.now())
                .build();

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .build();

        adicionarItensAoOrcamento(servicos, insumos, orcamento);

        ordemServico.setOrcamento(orcamento);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico buscarPorId(String id) {
        return findOrdemServico(id);
    }

    @Override
    public List<OrdemServico> listarAtendimentosEmAberto() {
        return ordemServicoGateway.buscarTodosPorStatusNotIn(
                        List.of(Status.FINALIZADA, Status.ENTREGUE, Status.CANCELADA))
                .stream()
                .sorted(Comparator
                        .comparing((OrdemServico os) -> PRIORIDADE_LISTAGEM.getOrDefault(os.getStatus(), Integer.MAX_VALUE))
                        .thenComparing(OrdemServico::getDataCriacao, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    @Override
    public List<TempoMedioExecucaoServico> listarTempoMedioExecucaoServicos() {
        Map<Long, TempoMedioServico> indicadores = new HashMap<>();
        List<OrdemServico> ordensConcluidas = ordemServicoGateway.buscarTodosPorStatusIn(
                List.of(Status.FINALIZADA, Status.ENTREGUE));

        for (OrdemServico ordemServico : ordensConcluidas) {
            if (ordemServico.getOrcamento() == null
                    || ordemServico.getOrcamento().getServicos() == null) {
                continue;
            }

            Long tempoExecucaoMinutos = calcularTempoExecucaoMinutos(ordemServico);
            if (tempoExecucaoMinutos == null) {
                continue;
            }

            for (OrdemServicoServico item : ordemServico.getOrcamento().getServicos()) {
                if (item.getServicoId() == null) {
                    continue;
                }

                TempoMedioServico indicador = indicadores.computeIfAbsent(
                        item.getServicoId(),
                        id -> new TempoMedioServico(item.getServicoId(), item.getNomeServico())
                );
                indicador.adicionar(tempoExecucaoMinutos);
            }
        }

        return indicadores.values().stream()
                .map(TempoMedioServico::toDomain)
                .sorted(Comparator.comparing(TempoMedioExecucaoServico::getNome,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    private Long calcularTempoExecucaoMinutos(OrdemServico ordemServico) {
        if (ordemServico.getHistoricoDeEventos() == null) {
            return null;
        }

        LocalDateTime inicioExecucao = buscarDataHoraEvento(ordemServico, Status.EM_EXECUCAO);
        LocalDateTime fimExecucao = buscarDataHoraEvento(ordemServico, Status.FINALIZADA);

        if (inicioExecucao == null || fimExecucao == null) {
            return null;
        }

        return Duration.between(inicioExecucao, fimExecucao).toMinutes();
    }

    private LocalDateTime buscarDataHoraEvento(OrdemServico ordemServico, Status status) {
        return ordemServico.getHistoricoDeEventos().stream()
                .filter(evento -> evento.getNovoStatus() == status)
                .map(TrocaStatus::getDataHora)
                .findFirst()
                .orElse(null);
    }

    @Override
    public OrdemServico iniciarDiagnostico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.EM_DIAGNOSTICO);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico realizarDiagnostico(String id, List<ServicoQuantidadeCommand> servicos,
                                             List<InsumoQuantidadeCommand> insumos, String observacoesDiagnostico) {
        OrdemServico ordemServico = findOrdemServico(id);

        if (ordemServico.getStatus() != Status.EM_DIAGNOSTICO) {
            throw new ValidacaoException("Apenas ordens em diagnóstico podem receber novos itens.");
        }

        if (observacoesDiagnostico != null) {
            ordemServico.setObservacoesDiagnostico(observacoesDiagnostico);
        }

        Orcamento orcamento = ordemServico.getOrcamento();

        adicionarItensAoOrcamento(servicos, insumos, orcamento);

        ordemServico.atualizarStatus(Status.AGUARDANDO_APROVACAO);
        notificarCliente(ordemServico, CodigoTemplate.AUTORIZAR_ORCAMENTO);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico aprovarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);

        if (ordemServico.getOrcamento() != null && ordemServico.getOrcamento().getInsumos() != null
                && !ordemServico.getOrcamento().getInsumos().isEmpty()) {
            List<EstoqueIntegracaoGateway.ItemDeducaoEstoque> itens = ordemServico.getOrcamento().getInsumos().stream()
                    .map(item -> new EstoqueIntegracaoGateway.ItemDeducaoEstoque(item.getInsumoId(), item.getQuantidade()))
                    .toList();
            try {
                estoqueIntegracaoGateway.deduzirEstoque(itens);
            } catch (EstoqueInsuficienteException ex) {
                notificationGateway.notificarFuncionarios(CodigoTemplate.REPOSICAO_ESTOQUE, ex.getMessage());
                throw ex;
            }
        }

        ordemServico.atualizarStatus(Status.EM_EXECUCAO);

        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico cancelarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.CANCELADA);
        return ordemServicoGateway.salvar(ordemServico);
    }

    @Override
    public OrdemServico finalizarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);

        ordemServico.atualizarStatus(Status.FINALIZADA);
        notificarCliente(ordemServico, CodigoTemplate.RETIRAR_VEICULO);

        return ordemServicoGateway.salvar(ordemServico);
    }

    private void adicionarItensAoOrcamento(List<ServicoQuantidadeCommand> servicosRequest,
                                            List<InsumoQuantidadeCommand> insumosRequest,
                                            Orcamento orcamento) {
        if (servicosRequest != null) {
            servicosRequest.forEach(sq -> {
                ServicoIntegracaoGateway.ServicoIntegracao servico = servicoIntegracaoGateway.buscarServico(sq.servico());
                orcamento.adicionarServico(servico.id(), servico.nome(), servico.valor(), sq.quantidade());
            });
        }

        if (insumosRequest != null) {
            insumosRequest.forEach(iq -> {
                EstoqueIntegracaoGateway.InsumoIntegracao insumo = estoqueIntegracaoGateway.buscarInsumo(iq.insumo());
                orcamento.adicionarInsumo(insumo.id(), insumo.nome(), insumo.precoUnitario(), iq.quantidade());
            });
        }
    }

    @Override
    public OrdemServico entregarVeiculo(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.ENTREGUE);
        notificarCliente(ordemServico, CodigoTemplate.VEICULO_RETIRADO);

        return ordemServicoGateway.salvar(ordemServico);
    }

    private void notificarCliente(OrdemServico ordemServico, CodigoTemplate codigoTemplate) {
        ClienteIntegracaoGateway.ClienteIntegracao cliente = clienteIntegracaoGateway.buscarCliente(ordemServico.getClienteId());
        notificationGateway.notificarCliente(codigoTemplate, cliente);
    }

    private OrdemServico findOrdemServico(String id) {
        return ordemServicoGateway.buscarPorId(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
    }

    private static class TempoMedioServico {
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

        private TempoMedioExecucaoServico toDomain() {
            long tempoMedioMinutos = BigDecimal.valueOf(tempoTotalMinutos)
                    .divide(BigDecimal.valueOf(ordensFinalizadas), 0, RoundingMode.HALF_UP)
                    .longValue();

            return TempoMedioExecucaoServico.builder()
                    .servicoId(servicoId)
                    .nome(nome)
                    .ordensFinalizadas(ordensFinalizadas)
                    .tempoMedioExecucaoMinutos(tempoMedioMinutos)
                    .build();
        }
    }
}
