package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

public interface RetryingTransactionService extends TransactionService, RetryingReadOnlyTransactionService {
    void inRetryingTransaction(Runnable command);

    <T> T inRetryingTransaction(Callable<T> command) throws Exception;
}
