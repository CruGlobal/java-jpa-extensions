package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

import org.springframework.transaction.annotation.Transactional;

public class DefaultTransactionService implements TransactionService {
    @Override
    @Transactional(readOnly = true)
    public void inReadOnlyTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T inReadOnlyTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }

    @Override
    @Transactional
    public void inTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @Transactional
    public <T> T inTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }
}
