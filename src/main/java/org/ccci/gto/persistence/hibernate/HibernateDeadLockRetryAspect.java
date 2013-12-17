package org.ccci.gto.persistence.hibernate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.aspectj.lang.annotation.Aspect;
import org.ccci.gto.persistence.DeadLockRetryAspect;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.exception.GenericJDBCException;

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
    protected boolean isDeadlock(final PersistenceException exception) {
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
}
