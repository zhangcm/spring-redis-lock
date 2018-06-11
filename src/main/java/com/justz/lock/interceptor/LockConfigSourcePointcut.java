package com.justz.lock.interceptor;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * A Pointcut that matches if the underlying {@link LockConfigSource}
 *  * has an attribute for a given method.
 */
abstract class LockConfigSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        LockConfigSource lcs = getLockConfigSource();
        return lcs != null && !CollectionUtils.isEmpty(lcs.getLockConfig(method, targetClass));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LockConfigSourcePointcut)) {
            return false;
        }
        LockConfigSourcePointcut otherPc = (LockConfigSourcePointcut) other;
        return ObjectUtils.nullSafeEquals(getLockConfigSource(), otherPc.getLockConfigSource());
    }

    @Override
    public int hashCode() {
        return LockConfigSourcePointcut.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + getLockConfigSource();
    }

    protected abstract LockConfigSource getLockConfigSource();
}
