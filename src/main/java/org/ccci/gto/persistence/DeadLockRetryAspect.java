package org.ccci.gto.persistence;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import javax.annotation.Nonnull;
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

    private int order = -1;

    private int defaultAttempts = 3;

    /**
     * Deadlock retry. The aspect applies to every service method with the
     * annotation {@link DeadLockRetry}
     * 
     * @param pjp
     *            the joinpoint
     * @param deadLockRetry
     *            the concurrency retry
     * @return
     * 
     * @throws Throwable
     *             the throwable
     */
    @Around(value = "@annotation(deadLockRetry)", argNames = "deadLockRetry")
    public Object concurrencyRetry(final ProceedingJoinPoint pjp, final DeadLockRetry deadLockRetry) throws Throwable {
        int attemptsRemaining = deadLockRetry.attempts();

        // loop until we complete successfully or have too many deadlock exceptions
        while (true) {
            try {
                // attempt to proceed
                return pjp.proceed();
            } catch (final PersistenceException e) {
                // handle deadlock exceptions when we still have attempts remaining
                if (attemptsRemaining > 0 && this.isDeadlock(e)) {
                    LOG.error("Deadlocked", e);
                    attemptsRemaining--;
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
     * @param exception
     *            the persitence error
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
