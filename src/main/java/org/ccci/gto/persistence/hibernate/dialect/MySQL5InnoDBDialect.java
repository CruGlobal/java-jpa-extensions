package org.ccci.gto.persistence.hibernate.dialect;

import org.ccci.gto.persistence.hibernate.ErrorCodeAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class MySQL5InnoDBDialect extends org.hibernate.dialect.MySQL5InnoDBDialect implements ErrorCodeAware {
    static final Collection<Integer> INNODB_DEADLOCK_ERROR_CODES = Collections.unmodifiableList(Arrays.asList(1205,
            1213));

    @Override
    public boolean isDeadlockErrorCode(final int error) {
        return INNODB_DEADLOCK_ERROR_CODES.contains(error);
    }
}
