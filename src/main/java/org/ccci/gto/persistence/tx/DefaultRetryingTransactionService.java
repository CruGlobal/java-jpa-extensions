package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

import org.ccci.gto.persistence.DeadLockRetry;
import org.springframework.transaction.annotation.Transactional;

public class DefaultRetryingTransactionService extends DefaultTransactionService implements RetryingTransactionService {
    @Override
    @DeadLockRetry
    @Transactional(readOnly = true)
    public void inRetryingReadOnlyTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @DeadLockRetry
    @Transactional(readOnly = true)
    public <T> T inRetryingReadOnlyTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }

    @Override
    @DeadLockRetry
    @Transactional
    public void inRetryingTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @DeadLockRetry
    @Transactional
    public <T> T inRetryingTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }
}
