package org.ccci.gto.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeadLockRetry {
    /**
     * Retry count. -1 indicates to use the default number of retries.
     */
    int attempts() default -1;

    /**
     * The name of the persistence unit to retry deadlocks on. This defaults to the default persistence unit.
     */
    String unitName() default "";
}
