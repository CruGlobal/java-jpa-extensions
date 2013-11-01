package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

public interface TransactionService extends ReadOnlyTransactionService {
    void inTransaction(Runnable command);

    <T> T inTransaction(Callable<T> command) throws Exception;
}
