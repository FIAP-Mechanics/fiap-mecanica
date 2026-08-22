package com.fiap.mecanica.adapter.out.transaction;

import com.fiap.mecanica.application.port.out.TransacaoGateway;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringTransactionAdapter implements TransacaoGateway {

    private final TransactionTemplate transactionTemplate;

    public SpringTransactionAdapter(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void executar(Runnable trabalho) {
        transactionTemplate.executeWithoutResult(status -> trabalho.run());
    }
}
