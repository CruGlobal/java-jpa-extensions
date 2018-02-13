package org.ccci.gto.persistence;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

public interface DeadLockDetector {
    boolean isDeadlock(@Nonnull EntityManager em, @Nonnull Throwable e);
}
