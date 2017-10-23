package org.ccci.gto.persistence.tx;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

public interface TransactionService extends ReadOnlyTransactionService {
    @Transactional
    default void inTransaction(@Nonnull final Runnable command) {
        command.run();
    }

    @Transactional
    default <T, X extends Throwable> T inTransaction(@Nonnull final Closure<T, X> command) throws X {
        return command.run();
    }
}
