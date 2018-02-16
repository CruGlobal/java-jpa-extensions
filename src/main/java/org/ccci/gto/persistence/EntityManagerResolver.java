package org.ccci.gto.persistence;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import java.util.Optional;

public interface EntityManagerResolver {
    @Nonnull
    default Optional<EntityManager> resolveEntityManager(@Nonnull String unitName) {
        return Optional.empty();
    }

    @Nonnull
    default Optional<EntityManager> defaultEntityManager() {
        return Optional.empty();
    }
}
