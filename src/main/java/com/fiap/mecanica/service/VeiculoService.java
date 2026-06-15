package com.fiap.mecanica.service;

import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.dto.VeiculoDto;
import com.fiap.mecanica.exception.VeiculoInativoException;
import com.fiap.mecanica.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.repository.VeiculoRepository;
import com.fiap.mecanica.exception.ValidacaoException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Locale;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class VeiculoService {
    private final VeiculoRepository repository;

    public Veiculo cadastrarVeiculo(Veiculo veiculo) {
        if (veiculo.getPlaca() != null) {
            veiculo.setPlaca(veiculo.getPlaca().toUpperCase(Locale.ROOT));
        }

        int ano = veiculo.getAno() == null ? 0 : veiculo.getAno();
        int maxAno = Year.now().getValue() + 1;
        if (ano < 1900 || ano > maxAno) {
            String mensagem = String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno);
            throw new ValidacaoException(mensagem);
        }

        if (veiculo.getPlaca() != null && repository.existsByPlaca(veiculo.getPlaca())) {
            throw new VeiculoJaCadastradoException(veiculo.getPlaca());
        }

        return repository.save(veiculo);
    }

    public Veiculo buscarVeiculoPorId(Long id) {
        Veiculo veiculo = repository.findById(id).orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        if (!veiculo.isAtivo()) throw new VeiculoInativoException(id);
        return veiculo;
    }

    public Veiculo atualizarVeiculo(Long id, VeiculoDto veiculoDto) {
        Veiculo veiculo = this.buscarVeiculoPorId(id);

        atualizaSeExistente(veiculoDto.marca(), veiculo::setMarca);
        atualizaSeExistente(veiculoDto.modelo(), veiculo::setModelo);
        
        if (veiculoDto.placa() != null) {
            String placaNormalizada = veiculoDto.placa().toUpperCase(Locale.ROOT);
            
            if (!placaNormalizada.equals(veiculo.getPlaca()) &&
                repository.existsByPlaca(placaNormalizada)) {
                throw new VeiculoJaCadastradoException(placaNormalizada);
            }
            
            veiculo.setPlaca(placaNormalizada);
        }
        
        if (veiculoDto.ano() != null) {
            int maxAno = Year.now().getValue() + 1;
            if (veiculoDto.ano() < 1900 || veiculoDto.ano() > maxAno) {
                String mensagem = String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno);
                throw new ValidacaoException(mensagem);
            }
            veiculo.setAno(veiculoDto.ano());
        }

        return repository.save(veiculo);
    }

    public Veiculo excluirVeiculo(Long id) {
        Veiculo veiculo = this.buscarVeiculoPorId(id);
        veiculo.setAtivo(false);
        return repository.save(veiculo);
    }

    public Veiculo reativarVeiculo(Long id) {

        Veiculo veiculo = repository.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));

        if (veiculo.isAtivo()) {
            throw new ValidacaoException(
                    "O veículo já está ativo."
            );
        }

        veiculo.setAtivo(true);

        return repository.save(veiculo);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) setter.accept(valor);
    }
}