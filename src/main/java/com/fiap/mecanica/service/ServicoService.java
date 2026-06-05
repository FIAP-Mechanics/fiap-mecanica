package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Servico;
import com.fiap.mecanica.dto.ServicoDto;
import com.fiap.mecanica.exception.ServicoInativoException;
import com.fiap.mecanica.exception.ServicoNotFound;
import com.fiap.mecanica.repository.ServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class ServicoService {
    private final ServicoRepository repository;

    public Servico cadastrarServico(Servico servico) {
        return repository.save(servico);
    }

    public Servico buscarServicoPorId(Long idServico) {
        Servico servico = repository.findById(idServico).orElseThrow(() -> new ServicoNotFound(idServico));
        if (!servico.isAtivo()) throw new ServicoInativoException(idServico);
        return servico;
    }

    public Servico atualizarServico(Long idServico, ServicoDto servicoDto) {
        Servico servico = this.buscarServicoPorId(idServico);

        atualizaSeExistente(servicoDto.nome(), servico::setNome);
        atualizaSeExistente(servicoDto.descricao(), servico::setDescricao);
        atualizaSeExistente(servicoDto.valor(), servico::setValor);
        atualizaSeExistente(servicoDto.insumos(), servico::atualizarInsumos);

        return repository.save(servico);
    }

    public Servico excluirServico(Long idServico) {
        Servico servico = this.buscarServicoPorId(idServico);
        servico.setAtivo(false);
        return repository.save(servico);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
