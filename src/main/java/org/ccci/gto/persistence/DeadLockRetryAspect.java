package org.ccci.gto.persistence;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.dao.ConcurrencyFailureException;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import java.util.Optional;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 *
 * <emf>Note that the aspect implements the Ordered interface so we can set the precedence of the aspect higher than
 * the transaction advice (we want a fresh transaction each time we retry).</emf>
 */
@Aspect
public abstract class DeadLockRetryAspect implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(DeadLockRetryAspect.class);

    private int order = LOWEST_PRECEDENCE;

    private int defaultAttempts = 3;

    @Autowired
    private EntityManagerResolver entityManagerResolver;

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
        return internalDeadlockRetry(pjp, deadlockRetry.unitName(), attempts);
    }

    /**
     * Deadlock retry. This method can be used for &gt;aop:advice /&lt; XML config.
     */
    public Object deadlockRetry(final ProceedingJoinPoint pjp) throws Throwable {
        return internalDeadlockRetry(pjp, "", defaultAttempts);
    }

    public Object deadlockRetry(final ProceedingJoinPoint pjp, @Nonnull final String unitName) throws Throwable {
        return internalDeadlockRetry(pjp, unitName, defaultAttempts);
    }

    @Nonnull
    private Optional<EntityManager> resolveEntityManager(@Nonnull final String unitName) {
        return "".equals(unitName) ? entityManagerResolver.defaultEntityManager() :
                entityManagerResolver.resolveEntityManager(unitName);
    }

    private Object internalDeadlockRetry(final ProceedingJoinPoint pjp, @Nonnull final String unitName, int attempts)
            throws Throwable {
        LOG.trace("entering DeadLockRetry");

        // loop until we complete successfully or have too many deadlock exceptions
        final EntityManager em = resolveEntityManager(unitName).orElse(null);
        final boolean inTransaction = em != null && em.isJoinedToTransaction();
        while (true) {
            try {
                // attempt to proceed
                return pjp.proceed();
            } catch (final Throwable e) {
                // handle deadlock exceptions when we still have attempts remaining
                if (em != null && !inTransaction && attempts > 0 && isDeadlock(em, e)) {
                    LOG.error("Deadlocked, attempts remaining: {}", attempts, e);
                    attempts--;
                    continue;
                }

                // propagate the caught exception
                throw e;
            }
        }
    }

    private boolean isDeadlock(@Nonnull final EntityManager em, @Nonnull final Throwable e) {
        return e instanceof OptimisticLockException || e instanceof ConcurrencyFailureException ||
                isImplDeadlock(em, e);
    }

    /**
     * check if the exception is a deadlock error.
     *
     * @param exception the persistence error
     * @return is a deadlock error
     */
    protected abstract boolean isImplDeadlock(@Nonnull EntityManager em, @Nonnull Throwable exception);

    public int getDefaultAttempts() {
        return defaultAttempts;
    }

    public void setDefaultAttempts(final int attempts) {
        this.defaultAttempts = attempts;
    }

    public void setEntityManagerResolver(@Nonnull final EntityManagerResolver resolver) {
        entityManagerResolver = resolver;
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
