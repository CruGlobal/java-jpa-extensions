package org.ccci.gto.persistence.hibernate;

import org.ccci.gto.persistence.DeadLockDetector;
import org.hibernate.JDBCException;
import org.hibernate.SessionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.jpa.HibernateEntityManagerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

public class HibernateDeadLockDetector implements DeadLockDetector {
    /**
     * check if the exception is a deadlock error.
     *
     * @param exception
     *            the persitence error
     * @return is a deadlock error
     */
    @Override
    public boolean isDeadlock(@Nonnull final EntityManager em, @Nonnull final Throwable exception) {
        if (exception instanceof PersistenceException) {
            final Dialect dialect = getDialect(em);
            if (dialect instanceof ErrorCodeAware) {
                final Throwable cause = exception.getCause();
                if (cause instanceof JDBCException) {
                    final int code = JdbcExceptionHelper.extractErrorCode(((JDBCException) cause).getSQLException());
                    return ((ErrorCodeAware) dialect).isDeadlockErrorCode(code);
                }
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
    private Dialect getDialect(@Nonnull final EntityManager em) {
        final EntityManagerFactory emf = em.getEntityManagerFactory();
        if (emf instanceof HibernateEntityManagerFactory) {
            final SessionFactory sessionFactory = ((HibernateEntityManagerFactory) emf).getSessionFactory();
            if (sessionFactory instanceof SessionFactoryImplementor) {
                return ((SessionFactoryImplementor) sessionFactory).getDialect();
            }
        }
        return null;
    }
}
