package org.ccci.gto.persistence.hibernate;

import org.ccci.gto.persistence.DeadLockDetector;
import org.hibernate.JDBCException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Optional;

public class HibernateDeadLockDetector implements DeadLockDetector {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateDeadLockDetector.class);

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
            final ErrorCodeAware dialect = getDialect(em)
                    .filter(ErrorCodeAware.class::isInstance).map(ErrorCodeAware.class::cast)
                    .orElse(null);
            if (dialect != null) {
                final Throwable cause = exception.getCause();
                if (cause instanceof JDBCException) {
                    final int code = JdbcExceptionHelper.extractErrorCode(((JDBCException) cause).getSQLException());
                    return dialect.isDeadlockErrorCode(code);
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
    @Nonnull
    private Optional<Dialect> getDialect(@Nonnull final EntityManager em) {
        final SessionFactoryImplementor sessionFactory;
        try {
            sessionFactory = em.getEntityManagerFactory()
                    .unwrap(SessionFactoryImplementor.class);
        } catch (final PersistenceException e) {
            LOG.debug("error unwrapping the SessionFactoryImplementor, so we can't determine the dialect", e);
            return Optional.empty();
        }

        return Optional.ofNullable(sessionFactory.getDialect());
    }
}
