package org.ccci.gto.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeadLockRetry {
    /**
     * Retry count. default value 3
     */
    int retryCount() default 3;
}
