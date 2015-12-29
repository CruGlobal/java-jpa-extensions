package org.ccci.gto.persistence;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 *
 * <emf>Note that the aspect implements the Ordered interface so we can set the precedence of the aspect higher than
 * the transaction advice (we want a fresh transaction each time we retry).</emf>
 */
@Aspect
public abstract class DeadLockRetryAspect implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(DeadLockRetryAspect.class);

    @PersistenceContext
    protected EntityManager em;

    private int order = LOWEST_PRECEDENCE;

    private int defaultAttempts = 3;

    /**
     * Deadlock retry. The aspect applies to every service method with the annotation {@link DeadLockRetry}
     *
     * @param pjp           the joinpoint
     * @param deadlockRetry the
     * @return the return value from the joinpoint
     * @throws Throwable any throwable thrown by the {@link ProceedingJoinPoint}
     */
    @Around(value = "@annotation(deadlockRetry)", argNames = "deadlockRetry")
    public Object deadlockRetry(final ProceedingJoinPoint pjp, final DeadLockRetry deadlockRetry) throws Throwable {
        int attempts = deadlockRetry.attempts();
        if (attempts == -1) {
            attempts = defaultAttempts;
        }
        return internalDeadlockRetry(pjp, attempts);
    }

    /**
     * Deadlock retry. This method is used for &gt;aop:advice /&lt; XML config.
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object deadlockRetry(final ProceedingJoinPoint pjp) throws Throwable {
        return internalDeadlockRetry(pjp, defaultAttempts);
    }

    private Object internalDeadlockRetry(final ProceedingJoinPoint pjp, int attempts) throws Throwable {
        LOG.trace("entering DeadLockRetry");

        // loop until we complete successfully or have too many deadlock exceptions
        final boolean inTransaction = em.isJoinedToTransaction();
        while (true) {
            try {
                // attempt to proceed
                return pjp.proceed();
            } catch (final PersistenceException e) {
                // handle deadlock exceptions when we still have attempts remaining
                if (!inTransaction && attempts > 0 && this.isDeadlock(e)) {
                    LOG.error("Deadlocked, attempts remaining: {}", attempts, e);
                    attempts--;
                    continue;
                }

                // propagate the caught exception
                throw e;
            }
        }
    }

    /**
     * check if the exception is a deadlock error.
     *
     * @param exception the persistence error
     * @return is a deadlock error
     */
    protected abstract boolean isDeadlock(@Nonnull PersistenceException exception);

    public int getDefaultAttempts() {
        return defaultAttempts;
    }

    public void setDefaultAttempts(final int attempts) {
        this.defaultAttempts = attempts;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Sets the order.
     * 
     * @param order
     *            the order to set
     */
    public void setOrder(final int order) {
        this.order = order;
    }
}
