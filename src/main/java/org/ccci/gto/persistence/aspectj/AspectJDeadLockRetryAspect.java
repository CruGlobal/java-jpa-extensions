package org.ccci.gto.persistence.aspectj;

import static org.ccci.gto.persistence.DeadLockRetry.DEFAULT_ATTEMPTS;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ccci.gto.persistence.DeadLockRetry;
import org.ccci.gto.persistence.DeadLockRetryAspectSupport;
import org.springframework.core.Ordered;

import javax.annotation.Nonnull;

/**
 * This Aspect will cause methods to retry if there is a notion of a deadlock.
 *
 * <emf>Note that the aspect implements the Ordered interface so we can set the precedence of the aspect higher than
 * the transaction advice (we want a fresh transaction each time we retry).</emf>
 */
@Aspect
public class AspectJDeadLockRetryAspect extends DeadLockRetryAspectSupport implements Ordered {
    /**
     * Deadlock retry. The aspect applies to every service method with the annotation {@link DeadLockRetry}
     *
     * @param pjp           the joinpoint
     * @param deadlockRetry the
     * @return the return value from the joinpoint
     * @throws Throwable any throwable thrown by the {@link ProceedingJoinPoint}
     */
    @Around(value = "@annotation(deadlockRetry)", argNames = "deadlockRetry")
    public Object deadlockRetry(final ProceedingJoinPoint pjp, final DeadLockRetry deadlockRetry) throws Throwable {
        return wrapJoinPoint(pjp::proceed, deadlockRetry.unitName(), deadlockRetry.attempts());
    }

    /**
     * Deadlock retry. This method can be used for &gt;aop:advice /&lt; XML config.
     */
    public Object deadlockRetry(final ProceedingJoinPoint pjp) throws Throwable {
        return deadlockRetry(pjp, "", DEFAULT_ATTEMPTS);
    }

    public Object deadlockRetry(final ProceedingJoinPoint pjp, @Nonnull final String unitName) throws Throwable {
        return deadlockRetry(pjp, unitName, DEFAULT_ATTEMPTS);
    }

    public Object deadlockRetry(final ProceedingJoinPoint pjp, @Nonnull final String unitName, final int attempts)
            throws Throwable {
        return wrapJoinPoint(pjp::proceed, unitName, attempts);
    }
}
