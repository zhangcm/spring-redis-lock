package com.justz.lock.interceptor;

import com.justz.lock.annotation.LockParam;
import org.springframework.core.MethodClassKey;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the default key generator for lock operation.
 */
public class SimpleKeyGenerator implements KeyGenerator {

    private final Map<Object, List<Integer>> lockParamIndexCache = new ConcurrentHashMap<>();

    public String generate(Class<?> targetClass, Method method, String keyPrefix, Object... params) {
        List<Integer> lockParamIndex = getLockParamIndex(targetClass, method);
        if (lockParamIndex.size() == 0) {
            throw new IllegalStateException("method must have one parameter with @LockParam at least, " +
                    "or specify the key instead");
        }
        List<String> lockParams = new ArrayList<>(lockParamIndex.size());
        for (Integer index : lockParamIndex) {
            Object param = params[index];
            if (param == null) {
                throw new IllegalArgumentException("param with @LockParam must be not null");
            }
            lockParams.add(param.toString());
        }
        String lockParamKey = StringUtils.collectionToDelimitedString(lockParams, "_");
        return StringUtils.hasText(keyPrefix) ? keyPrefix + "_" + lockParamKey : lockParamKey;
    }

    private List<Integer> getLockParamIndex(Class<?> targetClass, Method method) {
        MethodClassKey cacheKey = new MethodClassKey(method, targetClass);
        List<Integer> index = lockParamIndexCache.get(cacheKey);
        if (index == null) {
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            index = new ArrayList<>(paramAnnotations.length);
            for (int i = 0; i < paramAnnotations.length; i++) {
                Annotation[] annotations = paramAnnotations[i];
                if (annotations.length == 0) {
                    continue;
                }
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().isAssignableFrom(LockParam.class)) {
                        index.add(i);
                        break;
                    }
                }
            }
            lockParamIndexCache.put(cacheKey, index);
        }
        return index;
    }

}
