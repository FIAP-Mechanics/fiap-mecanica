package com.fiap.mecanica.infrastructure.configuration;

import com.fiap.mecanica.application.port.in.AtendimentoUseCase;
import com.fiap.mecanica.application.port.in.AutenticacaoUseCase;
import com.fiap.mecanica.application.port.in.ClienteUseCase;
import com.fiap.mecanica.application.port.in.EstoqueUseCase;
import com.fiap.mecanica.application.port.in.FuncionarioUseCase;
import com.fiap.mecanica.application.port.in.InsumoUseCase;
import com.fiap.mecanica.application.port.in.ServicoUseCase;
import com.fiap.mecanica.application.port.in.TemplateUseCase;
import com.fiap.mecanica.application.port.in.VeiculoUseCase;
import com.fiap.mecanica.application.port.in.VinculoVeiculoUseCase;
import com.fiap.mecanica.application.port.out.AutenticacaoGateway;
import com.fiap.mecanica.application.port.out.ClienteGateway;
import com.fiap.mecanica.application.port.out.ClienteVeiculoGateway;
import com.fiap.mecanica.application.port.out.CodificadorSenhaGateway;
import com.fiap.mecanica.application.port.out.EstoqueGateway;
import com.fiap.mecanica.application.port.out.FuncionarioGateway;
import com.fiap.mecanica.application.port.out.GeradorTokenGateway;
import com.fiap.mecanica.application.port.out.InsumoGateway;
import com.fiap.mecanica.application.port.out.NotificacaoGateway;
import com.fiap.mecanica.application.port.out.OrdemServicoGateway;
import com.fiap.mecanica.application.port.out.ServicoGateway;
import com.fiap.mecanica.application.port.out.TemplateGateway;
import com.fiap.mecanica.application.port.out.TransacaoGateway;
import com.fiap.mecanica.application.port.out.VeiculoGateway;
import com.fiap.mecanica.application.usecase.AtendimentoInteractor;
import com.fiap.mecanica.application.usecase.AutenticacaoInteractor;
import com.fiap.mecanica.application.usecase.ClienteInteractor;
import com.fiap.mecanica.application.usecase.EstoqueInteractor;
import com.fiap.mecanica.application.usecase.FuncionarioInteractor;
import com.fiap.mecanica.application.usecase.InsumoInteractor;
import com.fiap.mecanica.application.usecase.ServicoInteractor;
import com.fiap.mecanica.application.usecase.TemplateInteractor;
import com.fiap.mecanica.application.usecase.VeiculoInteractor;
import com.fiap.mecanica.application.usecase.VinculoVeiculoInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public AutenticacaoUseCase autenticacaoUseCase(
            AutenticacaoGateway autenticacaoGateway,
            GeradorTokenGateway geradorTokenGateway) {
        return new AutenticacaoInteractor(autenticacaoGateway, geradorTokenGateway);
    }

    @Bean
    public ClienteUseCase clienteUseCase(ClienteGateway clienteGateway) {
        return new ClienteInteractor(clienteGateway);
    }

    @Bean
    public VeiculoUseCase veiculoUseCase(VeiculoGateway veiculoGateway) {
        return new VeiculoInteractor(veiculoGateway);
    }

    @Bean
    public VinculoVeiculoUseCase vinculoVeiculoUseCase(
            ClienteGateway clienteGateway,
            VeiculoGateway veiculoGateway,
            ClienteVeiculoGateway clienteVeiculoGateway) {
        return new VinculoVeiculoInteractor(
                clienteGateway,
                veiculoGateway,
                clienteVeiculoGateway);
    }

    @Bean
    public FuncionarioUseCase funcionarioUseCase(
            FuncionarioGateway funcionarioGateway,
            CodificadorSenhaGateway codificadorSenhaGateway) {
        return new FuncionarioInteractor(funcionarioGateway, codificadorSenhaGateway);
    }

    @Bean
    public ServicoUseCase servicoUseCase(ServicoGateway servicoGateway) {
        return new ServicoInteractor(servicoGateway);
    }

    @Bean
    public InsumoUseCase insumoUseCase(InsumoGateway insumoGateway) {
        return new InsumoInteractor(insumoGateway);
    }

    @Bean
    public EstoqueUseCase estoqueUseCase(
            EstoqueGateway estoqueGateway,
            NotificacaoGateway notificacaoGateway,
            TransacaoGateway transacaoGateway) {
        return new EstoqueInteractor(
                estoqueGateway,
                notificacaoGateway,
                transacaoGateway);
    }

    @Bean
    public TemplateUseCase templateUseCase(TemplateGateway templateGateway) {
        return new TemplateInteractor(templateGateway);
    }

    @Bean
    public AtendimentoUseCase atendimentoUseCase(
            OrdemServicoGateway ordemServicoGateway,
            ClienteUseCase clienteUseCase,
            VeiculoUseCase veiculoUseCase,
            ServicoUseCase servicoUseCase,
            InsumoUseCase insumoUseCase,
            EstoqueUseCase estoqueUseCase,
            NotificacaoGateway notificacaoGateway) {
        return new AtendimentoInteractor(
                ordemServicoGateway,
                clienteUseCase,
                veiculoUseCase,
                servicoUseCase,
                insumoUseCase,
                estoqueUseCase,
                notificacaoGateway);
    }
}
