package com.fiap.mecanica.service;

import com.fiap.mecanica.controller.mapper.OrdemServicoMapper;
import com.fiap.mecanica.controller.request.IniciarAtendimentoRequest;
import com.fiap.mecanica.domain.*;
import com.fiap.mecanica.dto.OrdemServicoDto;
import com.fiap.mecanica.exception.OrdemServicoNaoEncontradaException;
import com.fiap.mecanica.exception.TransicaoInvalidaException;
import com.fiap.mecanica.exception.ValidacaoException;
import com.fiap.mecanica.repository.OrdemServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final InsumoService insumoService;
    private final EstoqueService estoqueService;

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
        return ordemServicoRepository.findAllByStatusNot(Status.ENTREGUE)
                .stream()
                .map(OrdemServicoMapper::toDto)
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
//      TODO: aqui seria adicionado uma notificação para o usuário
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

    public OrdemServicoDto finalizarOrdemServico(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.EM_EXECUCAO) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.FINALIZADA);
        }

        ordemServico.setStatus(Status.FINALIZADA);
//      TODO: aqui deveria acontecer uma notificação para o cliente retirar o veículo

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }

    public OrdemServicoDto entregarOrdemServico(String id) {
        OrdemServico ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException(id));

        if (ordemServico.getStatus() != Status.FINALIZADA) {
            throw new TransicaoInvalidaException(ordemServico.getStatus(), Status.ENTREGUE);
        }

        ordemServico.setStatus(Status.ENTREGUE);
//      TODO: aqui deveria acontecer uma notificação para o cliente de que o veículo foi retirado

        OrdemServico salva = ordemServicoRepository.save(ordemServico);
        return OrdemServicoMapper.toDto(salva);
    }
}
