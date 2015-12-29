package org.ccci.gto.persistence.hibernate.dialect;

import static org.ccci.gto.persistence.hibernate.dialect.MySQL5InnoDBDialect.INNODB_DEADLOCK_ERROR_CODES;

import org.ccci.gto.persistence.hibernate.ErrorCodeAware;

public class MySQL57InnoDBDialect extends org.hibernate.dialect.MySQL57InnoDBDialect implements ErrorCodeAware {
    @Override
    public boolean isDeadlockErrorCode(final int error) {
        return INNODB_DEADLOCK_ERROR_CODES.contains(error);
    }
}
