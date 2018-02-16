package org.ccci.gto.persistence.aopalliance;

import static org.ccci.gto.persistence.DeadLockRetry.DEFAULT_ATTEMPTS;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.ccci.gto.persistence.DeadLockRetry;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

public final class DeadLockRetryAdvisor extends DeadLockRetryMethodInterceptorFactory implements PointcutAdvisor {
    private final AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    private final MethodInterceptor defaultAdvice = invocation -> {
        final DeadLockRetry deadLockRetry = invocation.getMethod().getAnnotation(DeadLockRetry.class);
        if (deadLockRetry != null) {
            return wrapJoinPoint(invocation::proceed, deadLockRetry.unitName(), deadLockRetry.attempts());
        }

        return wrapJoinPoint(invocation::proceed, "", DEFAULT_ATTEMPTS);
    };

    public DeadLockRetryAdvisor() {
        pointcut.setExpression("@annotation(org.ccci.gto.persistence.DeadLockRetry)");
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return defaultAdvice;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
