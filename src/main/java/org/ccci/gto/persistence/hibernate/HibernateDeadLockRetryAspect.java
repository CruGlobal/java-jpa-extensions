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

@Aspect
public class HibernateDeadLockRetryAspect extends DeadLockRetryAspect {
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
                return ((ErrorCodeAware) dialect).isDeadlockErrorCode(errorCode);
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
        final EntityManagerFactory emf = em.getEntityManagerFactory();
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
