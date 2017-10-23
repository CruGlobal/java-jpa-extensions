package org.ccci.gto.persistence.tx;

import org.ccci.gto.persistence.DeadLockRetry;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

public interface RetryingReadOnlyTransactionService extends ReadOnlyTransactionService {
    @DeadLockRetry
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    default void inRetryingReadOnlyTransaction(@Nonnull final Runnable command) {
        command.run();
    }

    @DeadLockRetry
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    default <T, X extends Throwable> T inRetryingReadOnlyTransaction(@Nonnull final Closure<T, X> command) throws X {
        return command.run();
    }
}
