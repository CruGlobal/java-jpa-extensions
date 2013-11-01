package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

public interface RetryingReadOnlyTransactionService extends ReadOnlyTransactionService {
    void inRetryingReadOnlyTransaction(Runnable command);

    <T> T inRetryingReadOnlyTransaction(Callable<T> command) throws Exception;
}
