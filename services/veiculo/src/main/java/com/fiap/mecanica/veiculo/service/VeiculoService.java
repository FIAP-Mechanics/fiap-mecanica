package com.fiap.mecanica.veiculo.service;

import com.fiap.mecanica.veiculo.domain.Veiculo;
import com.fiap.mecanica.veiculo.dto.VeiculoDto;
import com.fiap.mecanica.veiculo.exception.ValidacaoException;
import com.fiap.mecanica.veiculo.exception.VeiculoInativoException;
import com.fiap.mecanica.veiculo.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.veiculo.exception.VeiculoNaoEncontradoException;
import com.fiap.mecanica.veiculo.repository.VeiculoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@AllArgsConstructor
@Service
public class VeiculoService {
    private final VeiculoRepository repository;

    public List<Veiculo> buscarTodos() {
        return repository.findAll();
    }

    public Veiculo cadastrarVeiculo(Veiculo veiculo) {
        veiculo.setPlaca(veiculo.getPlaca().toUpperCase(Locale.ROOT));

        int ano = veiculo.getAno();
        int maxAno = Year.now().getValue() + 1;
        if (ano < 1900 || ano > maxAno) {
            String mensagem = String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno);
            throw new ValidacaoException(mensagem);
        }

        if (repository.existsByPlaca(veiculo.getPlaca())) {
            throw new VeiculoJaCadastradoException(veiculo.getPlaca());
        }

        return repository.save(veiculo);
    }

    public Veiculo buscarVeiculoPorId(Long id) {
        Veiculo veiculo = repository.findById(id).orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        if (!veiculo.isAtivo()) throw new VeiculoInativoException(id);
        return veiculo;
    }

    public Veiculo buscarPorPlaca(String placa) {
        return repository.findByPlaca(placa.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ValidacaoException("Veículo com placa " + placa + " não encontrado."));
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
            throw new ValidacaoException("O veículo já está ativo.");
        }

        veiculo.setAtivo(true);
        return repository.save(veiculo);
    }

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) setter.accept(valor);
    }
}
