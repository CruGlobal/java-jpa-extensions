package org.ccci.gto.persistence.hibernate.dialect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ccci.gto.persistence.hibernate.ErrorCodeAware;

public class MySQL5InnoDBDialect extends org.hibernate.dialect.MySQL5InnoDBDialect implements ErrorCodeAware {
    private static final Set<Integer> DEADLOCK_ERROR_CODES = new HashSet<Integer>();
    static {
        DEADLOCK_ERROR_CODES.add(1205);
        DEADLOCK_ERROR_CODES.add(1213);
    }

    @Override
    public Set<Integer> getDeadlockErrorCodes() {
        return Collections.unmodifiableSet(DEADLOCK_ERROR_CODES);
    }
}
