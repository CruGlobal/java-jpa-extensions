package org.ccci.gto.persistence.hibernate.dialect;

import org.ccci.gto.persistence.hibernate.ErrorCodeAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class HSQLDialect extends org.hibernate.dialect.HSQLDialect implements ErrorCodeAware {
    private static final Collection<Integer> HSQL_DEADLOCK_ERROR_CODES = Collections.unmodifiableList(Arrays.asList
            (-4861, -4871));

    @Override
    public boolean isDeadlockErrorCode(final int error) {
        return HSQL_DEADLOCK_ERROR_CODES.contains(error);
    }
}
