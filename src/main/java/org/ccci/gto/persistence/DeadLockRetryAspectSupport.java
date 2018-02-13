package org.ccci.gto.persistence;

import static org.ccci.gto.persistence.DeadLockRetry.DEFAULT_ATTEMPTS;

import org.ccci.gto.persistence.tx.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.dao.ConcurrencyFailureException;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.validation.constraints.NotNull;
import java.util.Optional;

abstract class DeadLockRetryAspectSupport implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(DeadLockRetryAspectSupport.class);

    @NotNull
    @Autowired
    private EntityManagerResolver entityManagerResolver;

    private int defaultAttempts = 3;

    private int order = LOWEST_PRECEDENCE;

    public void setEntityManagerResolver(@Nonnull final EntityManagerResolver resolver) {
        entityManagerResolver = resolver;
    }

    public void setDefaultAttempts(final int attempts) {
        defaultAttempts = attempts;
    }

    @Override
    public int getOrder() {
        return order;
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

    @Nonnull
    protected final Optional<EntityManager> resolveEntityManager(@Nonnull final String unitName) {
        return "".equals(unitName) ? entityManagerResolver.defaultEntityManager() :
                entityManagerResolver.resolveEntityManager(unitName);
    }

    protected final <X extends Throwable> Object wrapJoinPoint(@Nonnull final Closure<Object, X> joinPoint,
                                                               @Nonnull final String unitName, int attempts) throws X {
        if (attempts == DEFAULT_ATTEMPTS) {
            attempts = defaultAttempts;
        }

        LOG.trace("entering DeadLockRetry");

        // loop until we complete successfully or have too many deadlock exceptions
        final EntityManager em = resolveEntityManager(unitName).orElse(null);
        final boolean inTransaction = em != null && em.isJoinedToTransaction();
        while (true) {
            try {
                // attempt to proceed
                return joinPoint.run();
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
}
