package org.ccci.gto.persistence.aopalliance;

import static org.ccci.gto.persistence.DeadLockRetry.DEFAULT_ATTEMPTS;

import org.aopalliance.intercept.MethodInterceptor;
import org.ccci.gto.persistence.DeadLockRetryAspectSupport;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class DeadLockRetryMethodInterceptorFactory extends DeadLockRetryAspectSupport {
    private final Map<String, MethodInterceptor> advice = new HashMap<>();

    public MethodInterceptor getAdvice(@Nonnull final String unitName) {
        return advice.computeIfAbsent(unitName, this::createAdvice);
    }

    private MethodInterceptor createAdvice(@Nonnull final String unitName) {
        return invocation -> wrapJoinPoint(invocation::proceed, unitName, DEFAULT_ATTEMPTS);
    }
}
