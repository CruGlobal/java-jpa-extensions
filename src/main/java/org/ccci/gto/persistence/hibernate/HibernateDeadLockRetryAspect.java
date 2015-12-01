package org.ccci.gto.persistence.hibernate;

import org.aspectj.lang.annotation.Aspect;
import org.ccci.gto.persistence.DeadLockRetryAspect;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.jpa.HibernateEntityManagerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

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
public class HibernateDeadLockRetryAspect extends DeadLockRetryAspect {
    @PersistenceUnit
    private EntityManagerFactory emf;

    /**
     * check if the exception is a deadlock error.
     * 
     * @param exception
     *            the persitence error
     * @return is a deadlock error
     */
    protected boolean isDeadlock(@Nonnull final PersistenceException exception) {
        final Dialect dialect = getDialect();
        if (dialect instanceof ErrorCodeAware) {
            final Throwable cause = exception.getCause();
            if (cause instanceof GenericJDBCException) {
                final int errorCode = getSQLErrorCode((GenericJDBCException) cause);
                return ((ErrorCodeAware) dialect).getDeadlockErrorCodes().contains(errorCode);
            }
        }
        return false;
    }

    /**
     * Returns the currently used dialect
     * 
     * @return the dialect
     */
    @Nullable
    private Dialect getDialect() {
        if (emf instanceof HibernateEntityManagerFactory) {
            final SessionFactory sessionFactory = ((HibernateEntityManagerFactory) emf).getSessionFactory();
            if (sessionFactory instanceof SessionFactoryImplementor) {
                return ((SessionFactoryImplementor) sessionFactory).getDialect();
            }
        }
        return null;
    }

    /**
     * extracts the low level sql error code from the
     * {@link PersistenceException}
     * 
     * @param exception
     *            the persistence exception
     * @return the low level sql error code
     */
    private int getSQLErrorCode(@Nonnull final GenericJDBCException exception) {
        return exception.getSQLException().getErrorCode();
    }
}
