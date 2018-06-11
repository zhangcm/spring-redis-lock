package com.justz.lock.interceptor;

import java.lang.reflect.Method;

/**
 * Lock key generator. Used for creating a key based on the given method
 * (used as context) and its parameters.
 */
public interface KeyGenerator {

    /**
     * Generate a key for the given method and its parameters.
     * @param targetClass the target instance class
     * @param method the method being called
     * @param keyPrefix the prefix of key
     * @param params the method parameters (with any var-args expanded)
     * @return a generated key
     */
    String generate(Class<?> targetClass, Method method, String keyPrefix, Object... params);

}
