package com.justz.lock.annotation;

import com.justz.lock.interceptor.LockOperationConfig;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Strategy interface for parsing known lock annotation types.
 */
public interface LockAnnotationParser {

    Collection<LockOperationConfig> parseLockAnnotations(Class<?> type);

    Collection<LockOperationConfig> parseLockAnnotations(Method method);

}
