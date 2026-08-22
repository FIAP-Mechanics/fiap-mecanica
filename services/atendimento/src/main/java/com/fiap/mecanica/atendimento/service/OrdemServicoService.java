package com.fiap.mecanica.atendimento.service;

import com.fiap.mecanica.atendimento.client.EstoqueClient;
import com.fiap.mecanica.atendimento.client.ClienteClient;
import com.fiap.mecanica.atendimento.client.ServicoClient;
import com.fiap.mecanica.atendimento.client.VeiculoClient;
import com.fiap.mecanica.atendimento.client.dto.ClienteIntegracaoDto;
import com.fiap.mecanica.atendimento.client.dto.DeduzirEstoqueItemDto;
import com.fiap.mecanica.atendimento.client.dto.EstoqueIntegracaoDto;
import com.fiap.mecanica.atendimento.client.dto.ServicoIntegracaoDto;
import com.fiap.mecanica.atendimento.controller.mapper.OrdemServicoMapper;
import com.fiap.mecanica.atendimento.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.atendimento.domain.OrdemServico;
import com.fiap.mecanica.atendimento.domain.OrdemServicoServico;
import com.fiap.mecanica.atendimento.domain.Orcamento;
import com.fiap.mecanica.atendimento.domain.Status;
import com.fiap.mecanica.atendimento.domain.TrocaStatus;
import com.fiap.mecanica.atendimento.dto.OrdemServicoDto;
import com.fiap.mecanica.atendimento.dto.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.atendimento.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.atendimento.exception.ValidacaoException;
import com.fiap.mecanica.atendimento.infra.enums.CodigoTemplate;
import com.fiap.mecanica.atendimento.repository.OrdemServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class OrdemServicoService {
    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteClient clienteClient;
    private final VeiculoClient veiculoClient;
    private final ServicoClient servicoClient;
    private final EstoqueClient estoqueClient;
    private final NotificationService notificationService;

    public OrdemServicoDto iniciarAtendimento(Long clienteId, Long veiculoId, String relatoCliente,
                                              List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest,
                                              List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest) {
        clienteClient.buscarCliente(clienteId);
        veiculoClient.buscarVeiculo(veiculoId);

        OrdemServico ordemServico = OrdemServicoMapper.toEntity(clienteId, veiculoId, relatoCliente);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .build();

        adicionarItensAoOrcamento(servicosRequest, insumosRequest, orcamento);

        ordemServico.setOrcamento(orcamento);
        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto buscarPorId(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        return OrdemServicoMapper.toDto(ordemServico);
    }

    public List<OrdemServicoDto> listarAtendimentosEmAberto() {
        return ordemServicoRepository.findAllByStatusNotIn(List.of(Status.ENTREGUE, Status.CANCELADA))
                .stream()
                .map(OrdemServicoMapper::toDto)
                .toList();
    }

    public List<TempoMedioExecucaoServicoDto> listarTempoMedioExecucaoServicos() {
        Map<Long, TempoMedioServico> indicadores = new HashMap<>();
        List<OrdemServico> ordensConcluidas = ordemServicoRepository.findAllByStatusIn(
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
                .map(TempoMedioServico::toDto)
                .sorted(Comparator.comparing(TempoMedioExecucaoServicoDto::nome,
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

    public OrdemServicoDto iniciarDiagnostico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.EM_DIAGNOSTICO);
        ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(ordemServico);
    }

    public OrdemServicoDto realizarDiagnostico(String id, List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest,
                                               List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest,
                                               String observacoesDiagnostico) {
        OrdemServico ordemServico = findOrdemServico(id);

        if (ordemServico.getStatus() != Status.EM_DIAGNOSTICO) {
            throw new ValidacaoException("Apenas ordens em diagnóstico podem receber novos itens.");
        }

        if (observacoesDiagnostico != null) {
            ordemServico.setObservacoesDiagnostico(observacoesDiagnostico);
        }

        Orcamento orcamento = ordemServico.getOrcamento();

        adicionarItensAoOrcamento(servicosRequest, insumosRequest, orcamento);

        ordemServico.atualizarStatus(Status.AGUARDANDO_APROVACAO);
        notificarCliente(ordemServico, CodigoTemplate.AUTORIZAR_ORCAMENTO);
        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto aprovarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);

        if (ordemServico.getOrcamento() != null && ordemServico.getOrcamento().getInsumos() != null
                && !ordemServico.getOrcamento().getInsumos().isEmpty()) {
            List<DeduzirEstoqueItemDto> itens = ordemServico.getOrcamento().getInsumos().stream()
                    .map(item -> new DeduzirEstoqueItemDto(item.getInsumoId(), item.getQuantidade()))
                    .toList();
            estoqueClient.deduzirEstoque(itens);
        }

        ordemServico.atualizarStatus(Status.EM_EXECUCAO);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto cancelarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.CANCELADA);
        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto finalizarOrdemServico(String id) {
        OrdemServico ordemServico = findOrdemServico(id);

        ordemServico.atualizarStatus(Status.FINALIZADA);
        notificarCliente(ordemServico, CodigoTemplate.RETIRAR_VEICULO);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    private void adicionarItensAoOrcamento(List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest,
                                           List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest,
                                           Orcamento orcamento) {
        if (servicosRequest != null) {
            servicosRequest.forEach(sq -> {
                ServicoIntegracaoDto servico = servicoClient.buscarServico(sq.servico());
                orcamento.adicionarServico(servico.id(), servico.nome(), servico.valor(), sq.quantidade());
            });
        }

        if (insumosRequest != null) {
            insumosRequest.forEach(iq -> {
                EstoqueIntegracaoDto estoque = estoqueClient.buscarInsumo(iq.insumo());
                orcamento.adicionarInsumo(estoque.insumo().id(), estoque.insumo().nome(), estoque.insumo().precoUnitario(), iq.quantidade());
            });
        }
    }

    public OrdemServicoDto entregarVeiculo(String id) {
        OrdemServico ordemServico = findOrdemServico(id);
        ordemServico.atualizarStatus(Status.ENTREGUE);
        notificarCliente(ordemServico, CodigoTemplate.VEICULO_RETIRADO);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    private void notificarCliente(OrdemServico ordemServico, CodigoTemplate codigoTemplate) {
        ClienteIntegracaoDto cliente = clienteClient.buscarCliente(ordemServico.getClienteId());
        notificationService.notificarCliente(codigoTemplate, cliente);
    }

    private OrdemServico findOrdemServico(String id) {
        return ordemServicoRepository.findById(id)
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

        private TempoMedioExecucaoServicoDto toDto() {
            long tempoMedioMinutos = BigDecimal.valueOf(tempoTotalMinutos)
                    .divide(BigDecimal.valueOf(ordensFinalizadas), 0, RoundingMode.HALF_UP)
                    .longValue();

            return TempoMedioExecucaoServicoDto.builder()
                    .servicoId(servicoId)
                    .nome(nome)
                    .ordensFinalizadas(ordensFinalizadas)
                    .tempoMedioExecucaoMinutos(tempoMedioMinutos)
                    .build();
        }
    }
}
