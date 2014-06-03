package org.ccci.gto.persistence.tx;

import org.ccci.gto.persistence.DeadLockRetry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.Callable;

public class DefaultRetryingTransactionService extends DefaultTransactionService implements RetryingTransactionService {
    @Override
    @DeadLockRetry
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void inRetryingReadOnlyTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @DeadLockRetry
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public <T> T inRetryingReadOnlyTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }

    @Override
    @DeadLockRetry
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inRetryingTransaction(final Runnable command) {
        command.run();
    }

    @Override
    @DeadLockRetry
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T inRetryingTransaction(final Callable<T> command) throws Exception {
        return command.call();
    }
}
