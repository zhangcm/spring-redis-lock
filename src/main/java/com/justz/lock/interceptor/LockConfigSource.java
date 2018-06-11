package com.justz.lock.interceptor;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Interface used by {@link LockInterceptor}. Implementations know how to source
 * lock operation attributes, whether from configuration, metadata attributes at
 * source level, or elsewhere.
 */
public interface LockConfigSource {

    /**
     * Return the collection of lock config for this method, or {@code null}
     * if the method contains no lock annotations.
     */
    Collection<LockOperationConfig> getLockConfig(Method method, Class<?> targetClass);

}
