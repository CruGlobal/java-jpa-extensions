package org.ccci.gto.persistence.tx;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

public interface ReadOnlyTransactionService {
    @Transactional(readOnly = true)
    default void inReadOnlyTransaction(@Nonnull final Runnable command) {
        command.run();
    }

    @Transactional(readOnly = true)
    default <T, X extends Throwable> T inReadOnlyTransaction(@Nonnull final Closure<T, X> command) throws X {
        return command.run();
    }
}
