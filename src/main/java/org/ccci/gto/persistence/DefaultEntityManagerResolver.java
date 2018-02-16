package org.ccci.gto.persistence;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

public class DefaultEntityManagerResolver implements EntityManagerResolver {
    @PersistenceContext
    private EntityManager entityManager;

    public void setEntityManager(final EntityManager manager) {
        entityManager = manager;
    }

    @Nonnull
    @Override
    public Optional<EntityManager> defaultEntityManager() {
        return Optional.ofNullable(entityManager);
    }
}
