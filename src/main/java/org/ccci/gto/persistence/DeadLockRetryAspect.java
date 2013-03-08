package org.ccci.gto.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.GenericJDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 * 
 * <emf>Note that the aspect implements the Ordered interface so we can set the
 * precedence of the aspect higher than the transaction advice (we want a fresh
 * transaction each time we retry).</emf>
 * 
 * @author Jelle Victoor
 * @version 04-jul-2011 handles deadlocks
 */
@Aspect
public class DeadLockRetryAspect implements Ordered {
    private static final Logger LOG = LoggerFactory.getLogger(DeadLockRetryAspect.class);

    private int order = -1;

    @PersistenceUnit
    private EntityManagerFactory emf;

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
        final Integer retryCount = deadLockRetry.retryCount();
        Integer deadlockCounter = 0;
        Object result = null;
        while (deadlockCounter < retryCount) {
            try {
                result = pjp.proceed();
                break;
            } catch (final PersistenceException exception) {
                deadlockCounter = handleException(exception, deadlockCounter, retryCount);
            }
        }
        return result;
    }

    /**
     * handles the persistence exception. Performs checks to see if the
     * exception is a deadlock and check the retry count.
     * 
     * @param exception
     *            the persistence exception that could be a deadlock
     * @param deadlockCounter
     *            the counter of occured deadlocks
     * @param retryCount
     *            the max retry count
     * @return the deadlockCounter that is incremented
     */
    private Integer handleException(final PersistenceException exception, Integer deadlockCounter,
            final Integer retryCount) {
        if (isDeadlock(exception)) {
            deadlockCounter++;
            LOG.error("Deadlocked", exception);
            if (deadlockCounter == (retryCount - 1)) {
                throw exception;
            }
        } else {
            throw exception;
        }

        return deadlockCounter;
    }

    /**
     * check if the exception is a deadlock error.
     * 
     * @param exception
     *            the persitence error
     * @return is a deadlock error
     */
    private boolean isDeadlock(final PersistenceException exception) {
        final Dialect dialect = getDialect();
        if (dialect instanceof ErrorCodeAware && exception.getCause() instanceof GenericJDBCException) {
            if (((ErrorCodeAware) dialect).getDeadlockErrorCodes().contains(getSQLErrorCode(exception))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the currently used dialect
     * 
     * @return the dialect
     */
    private Dialect getDialect() {
        final SessionFactory sessionFactory = ((HibernateEntityManagerFactory) emf).getSessionFactory();
        return ((SessionFactoryImplementor) sessionFactory).getDialect();
    }

    /**
     * extracts the low level sql error code from the
     * {@link PersistenceException}
     * 
     * @param exception
     *            the persistence exception
     * @return the low level sql error code
     */
    private int getSQLErrorCode(final PersistenceException exception) {
        return ((GenericJDBCException) exception.getCause()).getSQLException().getErrorCode();
    }

    /** {@inheritDoc} */
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
