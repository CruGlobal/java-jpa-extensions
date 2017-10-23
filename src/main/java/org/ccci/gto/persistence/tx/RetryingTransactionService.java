package org.ccci.gto.persistence.tx;

import org.ccci.gto.persistence.DeadLockRetry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

public interface RetryingTransactionService extends TransactionService, RetryingReadOnlyTransactionService {
    @DeadLockRetry
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default void inRetryingTransaction(@Nonnull final Runnable command) {
        command.run();
    }

    @DeadLockRetry
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    default <T, X extends Throwable> T inRetryingTransaction(@Nonnull final Closure<T, X> command) throws X {
        return command.run();
    }
}
