package com.justz.lock.interceptor;

import com.justz.lock.LockManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * AOP Alliance MethodInterceptor for declarative lock
 * management using the lock infrastructure ({@link com.justz.lock.annotation.Lock})
 */
public class LockInterceptor extends LockAspectSupport implements MethodInterceptor, Serializable {

    private LockConfigSource lockConfigSource;

    public Object invoke(final MethodInvocation invocation) throws Throwable {

        Method method = invocation.getMethod();

        LockOperationInvoker invoker = new LockOperationInvoker() {
            @Override
            public Object invoke() throws ThrowableWrapper {
                try {
                    return invocation.proceed();
                } catch (Throwable ex) {
                    throw new LockOperationInvoker.ThrowableWrapper(ex);
                }
            }
        };

        try {
            return execute(invoker, invocation.getThis(), method, invocation.getArguments());
        } catch (LockOperationInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }

    public void setLockConfigSource(LockConfigSource lockConfigSource) {
        this.lockConfigSource = lockConfigSource;
    }


    @Override
    protected LockConfigSource getLockConfigSource() {
        return lockConfigSource;
    }

    public void setLockManager(LockManager lockManager) {

    }
}
