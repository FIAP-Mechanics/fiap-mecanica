package com.fiap.mecanica.service;

import com.fiap.mecanica.controller.mapper.OrdemServicoMapper;
import com.fiap.mecanica.controller.request.FinalizarOrdemServicoRequest;
import com.fiap.mecanica.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.dto.TempoMedioExecucaoServicoDto;
import com.fiap.mecanica.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.exception.TransicaoInvalidaException;
import com.fiap.mecanica.exception.ValidacaoException;
import com.fiap.mecanica.infra.configs.enums.CodigoTemplate;
import com.fiap.mecanica.repository.OrdemServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class OrdemServicoService {

    private static final List<Status> STATUS_ENCERRADOS = List.of(Status.ENTREGUE, Status.CANCELADA);

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final InsumoService insumoService;
    private final EstoqueService estoqueService;
    private final NotificationService notificationService;

    public OrdemServicoDto iniciarAtendimento(Long clienteId, Long veiculoId, String relatoCliente,
                                              List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest,
                                              List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest) {
        Cliente cliente = clienteService.buscarClientePorId(clienteId);
        Veiculo veiculo = veiculoService.buscarVeiculoPorId(veiculoId);

        OrdemServico ordemServico = OrdemServicoMapper.toEntity(cliente, veiculo, relatoCliente);

        Orcamento orcamento = Orcamento.builder()
                .ordemServico(ordemServico)
                .build();

        if (servicosRequest != null && !servicosRequest.isEmpty()) {
            List<OrdemServicoServico> servicos = new ArrayList<>();
            for (IniciarAtendimentoRequest.ServicoQuantidade sq : servicosRequest) {
                Servico servico = servicoService.buscarServicoPorId(sq.servico());
                servicos.stream()
                        .filter(s -> s.getServico().getId().equals(sq.servico()))
                        .findFirst()
                        .ifPresentOrElse(
                                s -> s.setQuantidade(s.getQuantidade() + sq.quantidade()),
                                () -> servicos.add(OrdemServicoMapper.toServicoEntity(orcamento, servico, sq.quantidade()))
                        );
            }
            orcamento.setServicos(servicos);
        }

        if (insumosRequest != null && !insumosRequest.isEmpty()) {
            List<OrdemServicoInsumo> insumos = new ArrayList<>();
            for (IniciarAtendimentoRequest.InsumoQuantidade iq : insumosRequest) {
                Insumo insumo = insumoService.buscarInsumoPorId(iq.insumo());
                insumos.stream()
                        .filter(i -> i.getInsumo().getId().equals(iq.insumo()))
                        .findFirst()
                        .ifPresentOrElse(
                                i -> i.setQuantidade(i.getQuantidade() + iq.quantidade()),
                                () -> insumos.add(OrdemServicoMapper.toInsumoEntity(orcamento, insumo, iq.quantidade()))
                        );
            }
            orcamento.setInsumos(insumos);
        }

        orcamento.recalcularPrecoTotal();
        ordemServico.setOrcamento(orcamento);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto buscarPorId(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
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

            for (OrdemServicoServico ordemServicoServico : ordemServico.getOrcamento().getServicos()) {
                Servico servico = ordemServicoServico.getServico();
                if (servico == null || servico.getId() == null
                        || ordemServicoServico.getTempoExecucaoMinutos() == null) {
                    continue;
                }

                TempoMedioServico indicador = indicadores.computeIfAbsent(
                        servico.getId(),
                        id -> new TempoMedioServico(servico.getId(), servico.getNome())
                );
                indicador.adicionar(ordemServicoServico.getTempoExecucaoMinutos());
            }
        }

        return indicadores.values().stream()
                .map(TempoMedioServico::toDto)
                .sorted(Comparator.comparing(TempoMedioExecucaoServicoDto::nome,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    public OrdemServicoDto iniciarDiagnostico(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));
        if (ordemServico.getStatus() != Status.RECEBIDA) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.EM_DIAGNOSTICO);
        }
        ordemServico.setStatus(Status.EM_DIAGNOSTICO);
        ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(ordemServico);
    }

    public OrdemServicoDto realizarDiagnostico(String id, List<IniciarAtendimentoRequest.ServicoQuantidade> servicosRequest,
                                               List<IniciarAtendimentoRequest.InsumoQuantidade> insumosRequest,
                                               String observacoesDiagnostico) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.EM_DIAGNOSTICO) {
            throw new ValidacaoException("Apenas ordens em diagnóstico podem receber novos itens.");
        }

        if (observacoesDiagnostico != null) {
            ordemServico.setObservacoesDiagnostico(observacoesDiagnostico);
        }

        Orcamento orcamento = ordemServico.getOrcamento();

        if (servicosRequest != null && !servicosRequest.isEmpty()) {
            for (IniciarAtendimentoRequest.ServicoQuantidade sq : servicosRequest) {
                Servico servico = servicoService.buscarServicoPorId(sq.servico());
                orcamento.getServicos().stream()
                        .filter(s -> s.getServico().getId().equals(sq.servico()))
                        .findFirst()
                        .ifPresentOrElse(
                                s -> s.setQuantidade(s.getQuantidade() + sq.quantidade()),
                                () -> orcamento.getServicos().add(OrdemServicoMapper.toServicoEntity(orcamento, servico, sq.quantidade()))
                        );
            }
        }

        if (insumosRequest != null && !insumosRequest.isEmpty()) {
            for (IniciarAtendimentoRequest.InsumoQuantidade iq : insumosRequest) {
                Insumo insumo = insumoService.buscarInsumoPorId(iq.insumo());
                orcamento.getInsumos().stream()
                        .filter(i -> i.getInsumo().getId().equals(iq.insumo()))
                        .findFirst()
                        .ifPresentOrElse(
                                i -> i.setQuantidade(i.getQuantidade() + iq.quantidade()),
                                () -> orcamento.getInsumos().add(OrdemServicoMapper.toInsumoEntity(orcamento, insumo, iq.quantidade()))
                        );
            }
        }

        orcamento.recalcularPrecoTotal();
        ordemServico.setStatus(Status.AGUARDANDO_APROVACAO);
        notificationService.notificarCliente(CodigoTemplate.AUTORIZAR_ORCAMENTO, ordemServico.getCliente());
        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto aprovarOrdemServico(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.AGUARDANDO_APROVACAO) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.EM_EXECUCAO);
        }

        if (ordemServico.getOrcamento() != null && ordemServico.getOrcamento().getInsumos() != null) {
            estoqueService.deduzirEstoque(ordemServico.getOrcamento().getInsumos());
        }

        ordemServico.setStatus(Status.EM_EXECUCAO);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto cancelarOrdemServico(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.AGUARDANDO_APROVACAO) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.CANCELADA);
        }

        ordemServico.setStatus(Status.CANCELADA);

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto finalizarOrdemServico(String id, List<FinalizarOrdemServicoRequest.ServicoTempo> servicosTempo) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.EM_EXECUCAO) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.FINALIZADA);
        }

        registrarTempoExecucaoServicos(ordemServico, servicosTempo);
        ordemServico.setStatus(Status.FINALIZADA);
        notificationService.notificarCliente(CodigoTemplate.RETIRAR_VEICULO, ordemServico.getCliente());

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    private void registrarTempoExecucaoServicos(
            OrdemServico ordemServico,
            List<FinalizarOrdemServicoRequest.ServicoTempo> servicosTempo) {

        if (servicosTempo == null || servicosTempo.isEmpty()) {
            throw new ValidacaoException("Informe o tempo gasto nos servicos da ordem de servico.");
        }

        if (ordemServico.getOrcamento() == null || ordemServico.getOrcamento().getServicos() == null
                || ordemServico.getOrcamento().getServicos().isEmpty()) {
            throw new ValidacaoException("A ordem de servico nao possui servicos para registrar tempo.");
        }

        for (FinalizarOrdemServicoRequest.ServicoTempo servicoTempo : servicosTempo) {
            OrdemServicoServico servicoDaOrdem = ordemServico.getOrcamento().getServicos().stream()
                    .filter(item -> item.getServico() != null
                            && item.getServico().getId().equals(servicoTempo.servico()))
                    .findFirst()
                    .orElseThrow(() -> new ValidacaoException(
                            "Servico " + servicoTempo.servico() + " nao pertence a ordem de servico."));

            servicoDaOrdem.setTempoExecucaoMinutos(servicoTempo.tempoGastoMinutos());
        }

        boolean existeServicoSemTempo = ordemServico.getOrcamento().getServicos().stream()
                .anyMatch(item -> item.getTempoExecucaoMinutos() == null);

        if (existeServicoSemTempo) {
            throw new ValidacaoException("Informe o tempo gasto de todos os servicos da ordem de servico.");
        }
    }

    public OrdemServicoDto entregarVeiculo(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.FINALIZADA) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.ENTREGUE);
        }

        ordemServico.setStatus(Status.ENTREGUE);
        notificationService.notificarCliente(CodigoTemplate.VEICULO_RETIRADO, ordemServico.getCliente());

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
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
            BigDecimal tempoMedioMinutos = BigDecimal.valueOf(tempoTotalMinutos)
                    .divide(BigDecimal.valueOf(ordensFinalizadas), 2, RoundingMode.HALF_UP);

            return TempoMedioExecucaoServicoDto.builder()
                    .servicoId(servicoId)
                    .nome(nome)
                    .ordensFinalizadas(ordensFinalizadas)
                    .tempoMedioExecucaoMinutos(tempoMedioMinutos)
                    .build();
        }
    }
}
