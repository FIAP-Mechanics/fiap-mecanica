package com.fiap.mecanica.application.usecase;

import com.fiap.mecanica.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.domain.Veiculo;
import com.fiap.mecanica.exception.ValidacaoException;
import com.fiap.mecanica.exception.VeiculoInativoException;
import com.fiap.mecanica.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.exception.VeiculoNaoEncontradoException;

import java.time.Year;
import java.util.Locale;
import java.util.function.Consumer;

public class VeiculoInteractor implements VeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public VeiculoInteractor(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    @Override
    public Veiculo cadastrarVeiculo(Veiculo veiculo) {
        veiculo.setPlaca(veiculo.getPlaca().toUpperCase(Locale.ROOT));

        int ano = veiculo.getAno();
        int maxAno = Year.now().getValue() + 1;
        if (ano < 1900 || ano > maxAno) {
            throw new ValidacaoException(
                    String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno));
        }

        if (veiculoGateway.existePorPlaca(veiculo.getPlaca())) {
            throw new VeiculoJaCadastradoException(veiculo.getPlaca());
        }
        return veiculoGateway.salvar(veiculo);
    }

    @Override
    public Veiculo buscarVeiculoPorId(Long id) {
        Veiculo veiculo = veiculoGateway.buscarPorId(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        if (!veiculo.isAtivo()) {
            throw new VeiculoInativoException(id);
        }
        return veiculo;
    }

    @Override
    public Veiculo atualizarVeiculo(Long id, AtualizarVeiculoCommand command) {
        Veiculo veiculo = buscarVeiculoPorId(id);

        atualizarSeExistente(command.marca(), veiculo::setMarca);
        atualizarSeExistente(command.modelo(), veiculo::setModelo);

        if (command.placa() != null) {
            String placaNormalizada = command.placa().toUpperCase(Locale.ROOT);
            if (!placaNormalizada.equals(veiculo.getPlaca())
                    && veiculoGateway.existePorPlaca(placaNormalizada)) {
                throw new VeiculoJaCadastradoException(placaNormalizada);
            }
            veiculo.setPlaca(placaNormalizada);
        }

        if (command.ano() != null) {
            int maxAno = Year.now().getValue() + 1;
            if (command.ano() < 1900 || command.ano() > maxAno) {
                throw new ValidacaoException(
                        String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno));
            }
            veiculo.setAno(command.ano());
        }

        return veiculoGateway.salvar(veiculo);
    }

    @Override
    public Veiculo excluirVeiculo(Long id) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        veiculo.setAtivo(false);
        return veiculoGateway.salvar(veiculo);
    }

    @Override
    public Veiculo reativarVeiculo(Long id) {
        Veiculo veiculo = veiculoGateway.buscarPorId(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        if (veiculo.isAtivo()) {
            throw new ValidacaoException("O veículo já está ativo.");
        }
        veiculo.setAtivo(true);
        return veiculoGateway.salvar(veiculo);
    }

    private <T> void atualizarSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) {
            setter.accept(valor);
        }
    }
}
