package com.fiap.mecanica.servico.service;

import com.fiap.mecanica.servico.domain.Servico;
import com.fiap.mecanica.servico.dto.ServicoDto;
import com.fiap.mecanica.servico.exception.ServicoInativoException;
import com.fiap.mecanica.servico.exception.ServicoJaAtivoException;
import com.fiap.mecanica.servico.exception.ServicoNotFound;
import com.fiap.mecanica.servico.repository.ServicoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class ServicoService {
    private final ServicoRepository repository;

    public List<Servico> buscarTodos() {
        return repository.findAll();
    }

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
        return repository.save(servico);
    }

    public Servico excluirServico(Long idServico) {
        Servico servico = this.buscarServicoPorId(idServico);
        servico.setAtivo(false);
        return repository.save(servico);
    }

    public Servico reativarServico(Long idServico) {
        Servico servico = repository.findById(idServico).orElseThrow(() -> new ServicoNotFound(idServico));
        if (servico.isAtivo()) throw new ServicoJaAtivoException(idServico);
        servico.setAtivo(true);
        return repository.save(servico);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
