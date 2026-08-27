package com.fiap.mecanica.veiculo.application.usecase;

import com.fiap.mecanica.veiculo.application.command.AtualizarVeiculoCommand;
import com.fiap.mecanica.veiculo.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.veiculo.application.port.out.VeiculoGateway;
import com.fiap.mecanica.veiculo.domain.Veiculo;
import com.fiap.mecanica.veiculo.exception.ValidacaoException;
import com.fiap.mecanica.veiculo.exception.VeiculoInativoException;
import com.fiap.mecanica.veiculo.exception.VeiculoJaCadastradoException;
import com.fiap.mecanica.veiculo.exception.VeiculoNaoEncontradoException;

import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class VeiculoInteractor implements VeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public VeiculoInteractor(VeiculoGateway veiculoGateway) {
        this.veiculoGateway = veiculoGateway;
    }

    @Override
    public List<Veiculo> buscarTodos() {
        return veiculoGateway.buscarTodos();
    }

    @Override
    public Veiculo cadastrarVeiculo(Veiculo veiculo) {
        veiculo.setPlaca(veiculo.getPlaca().toUpperCase(Locale.ROOT));

        int ano = veiculo.getAno();
        int maxAno = Year.now().getValue() + 1;
        if (ano < 1900 || ano > maxAno) {
            String mensagem = String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno);
            throw new ValidacaoException(mensagem);
        }

        if (veiculoGateway.existsByPlaca(veiculo.getPlaca())) {
            throw new VeiculoJaCadastradoException(veiculo.getPlaca());
        }

        return veiculoGateway.salvar(veiculo);
    }

    @Override
    public Veiculo buscarVeiculoPorId(Long id) {
        Veiculo veiculo = veiculoGateway.buscarPorId(id).orElseThrow(() -> new VeiculoNaoEncontradoException(id));
        if (!veiculo.isAtivo()) throw new VeiculoInativoException(id);
        return veiculo;
    }

    @Override
    public Veiculo buscarPorPlaca(String placa) {
        return veiculoGateway.buscarPorPlaca(placa.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ValidacaoException("Veículo com placa " + placa + " não encontrado."));
    }

    @Override
    public Veiculo atualizarVeiculo(Long id, AtualizarVeiculoCommand command) {
        Veiculo veiculo = this.buscarVeiculoPorId(id);

        atualizaSeExistente(command.marca(), veiculo::setMarca);
        atualizaSeExistente(command.modelo(), veiculo::setModelo);

        if (command.placa() != null) {
            String placaNormalizada = command.placa().toUpperCase(Locale.ROOT);

            if (!placaNormalizada.equals(veiculo.getPlaca()) &&
                    veiculoGateway.existsByPlaca(placaNormalizada)) {
                throw new VeiculoJaCadastradoException(placaNormalizada);
            }

            veiculo.setPlaca(placaNormalizada);
        }

        if (command.ano() != null) {
            int maxAno = Year.now().getValue() + 1;
            if (command.ano() < 1900 || command.ano() > maxAno) {
                String mensagem = String.format("Ano inválido. Deve estar entre 1900 e %d.", maxAno);
                throw new ValidacaoException(mensagem);
            }
            veiculo.setAno(command.ano());
        }

        return veiculoGateway.salvar(veiculo);
    }

    @Override
    public Veiculo excluirVeiculo(Long id) {
        Veiculo veiculo = this.buscarVeiculoPorId(id);
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

    private <T> void atualizaSeExistente(T valor, Consumer<T> setter) {
        if (valor != null) setter.accept(valor);
    }
}
