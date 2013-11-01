package org.ccci.gto.persistence.tx;

import java.util.concurrent.Callable;

public interface ReadOnlyTransactionService {
    void inReadOnlyTransaction(Runnable command);

    <T> T inReadOnlyTransaction(Callable<T> command) throws Exception;
}
