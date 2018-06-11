package com.justz.lock.interceptor;

import com.justz.lock.annotation.Lock;
import com.justz.lock.annotation.LockAnnotationParser;
import com.justz.lock.annotation.LockConfig;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Strategy implementation for parsing {@link Lock}
 */
public class SpringLockAnnotationParser implements LockAnnotationParser, Serializable {

    @Override
    public Collection<LockOperationConfig> parseLockAnnotations(Class<?> type) {
        DefaultLockConfig defaultConfig = getDefaultLockConfig(type);
        return parseLockAnnotations(defaultConfig, type);
    }

    @Override
    public Collection<LockOperationConfig> parseLockAnnotations(Method method) {
        DefaultLockConfig defaultConfig = getDefaultLockConfig(method.getDeclaringClass());
        return parseLockAnnotations(defaultConfig, method);
    }

    protected Collection<LockOperationConfig> parseLockAnnotations(DefaultLockConfig lockConfig, AnnotatedElement ae) {
        Collection<LockOperationConfig> cfgs = null;
        Collection<Lock> locks = AnnotatedElementUtils.getAllMergedAnnotations(ae, Lock.class);
        if (!locks.isEmpty()) {
            cfgs = new ArrayList<>(1);
            for (Lock lock : locks) {
                cfgs.add(parseLockAnnotation(ae, lockConfig, lock));
            }
        }
        return cfgs;
    }

    LockOperationConfig parseLockAnnotation(AnnotatedElement ae, DefaultLockConfig defaultConfig, Lock lock) {
        LockOperationConfig config = new LockOperationConfig();
        config.setName(ae.toString());
        config.setKey(lock.key());
        config.setPrefix(lock.prefix());
        config.setKeyGenerator(lock.keyGenerator());
        config.setLockManager(lock.lockManager());
        config.setMaxWaitMillSeconds(lock.maxWaitMillSeconds());
        config.setActionAfterAcquireFailed(lock.actionAfterAcquireFailed());

        defaultConfig.applyDefault(config);

        validateLockOperationConfig(ae, config);
        return config;
    }

    DefaultLockConfig getDefaultLockConfig(Class<?> target) {
        LockConfig annotation = AnnotatedElementUtils.getMergedAnnotation(target, LockConfig.class);
        if (annotation != null) {
            return new DefaultLockConfig(annotation.keyGenerator(), annotation.lockManager());
        }
        return new DefaultLockConfig();
    }

    private void validateLockOperationConfig(AnnotatedElement ae, LockOperationConfig config) {
        if(StringUtils.hasText(config.getKey()) && StringUtils.hasText(config.getKeyGenerator())) {
            throw new IllegalStateException("Invalid lock annotation configuration on '" +
                    ae.toString() + "'. Both 'key' and 'keyGenerator' attributes have been set. " +
                    "These attributes are mutually exclusive: either set the SpEL expression used to" +
                    "compute the key at runtime or set the name of the KeyGenerator bean to use.");
        }
    }

    static class DefaultLockConfig {

        private final String keyGenerator;

        private final String lockManager;

        DefaultLockConfig() {
            this(null, null);
        }

        private DefaultLockConfig(String keyGenerator, String lockManager) {
            this.keyGenerator = keyGenerator;
            this.lockManager = lockManager;
        }

        void applyDefault(LockOperationConfig config) {
            if (!StringUtils.hasText(config.getKey()) && !StringUtils.hasText(config.getKeyGenerator()) &&
                    StringUtils.hasText(this.keyGenerator)) {
                config.setKeyGenerator(this.keyGenerator);
            }
            if (!StringUtils.hasText(config.getLockManager()) && StringUtils.hasText(this.lockManager)) {
                config.setLockManager(this.lockManager);
            }
        }
    }

}
