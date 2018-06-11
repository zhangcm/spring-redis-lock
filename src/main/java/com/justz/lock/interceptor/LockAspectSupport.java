package com.justz.lock.interceptor;

import com.justz.lock.LockManager;
import com.justz.lock.enumeration.ActionType;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.justz.lock.config.LockManagementConfigUtils.SIMPLE_LOCK_MANAGER_BEAN_NAME;

/**
 * Base class for lock aspects, such as the {@link LockInterceptor}
 */
public abstract class LockAspectSupport implements BeanFactoryAware, InitializingBean {

    private KeyGenerator keyGenerator = new SimpleKeyGenerator();

    private LockManager lockManager = null;

    private final Map<LockConfigCacheKey, LockConfigMetadata> metadataCache =
            new ConcurrentHashMap<>(1024);

    private BeanFactory beanFactory;

    protected Object execute(LockOperationInvoker invoker, Object target, Method method, Object[] args) {
        Class<?> targetClass = getTargetClass(target);
        Collection<LockOperationConfig> lockCfgs = getLockConfigSource().getLockConfig(method, targetClass);
        if (CollectionUtils.isEmpty(lockCfgs)) {
            return invoker.invoke();
        }
        return execute(invoker, lockCfgs, targetClass, method, args);
    }

    private Object execute(LockOperationInvoker invoker, Collection<LockOperationConfig> lockCfgs,
                           Class<?> targetClass, Method method, Object[] args) {
        LockOperationConfig lockCfg = lockCfgs.iterator().next();
        String key = generateLockKey(lockCfg, method, targetClass, args);
        LockManager lockManager = getLockManager(lockCfg, method, targetClass);
        boolean locked = false;
        try {
            locked = lockManager.lock(key, lockCfg.getMaxWaitMillSeconds());
            if (locked) {
                return invoker.invoke();
            } else if (lockCfg.getActionAfterAcquireFailed() == ActionType.EXECUTE) {
                return invoker.invoke();
            } else if (lockCfg.getActionAfterAcquireFailed() == ActionType.RETURN_NULL) {
                return null;
            } else {
                throw new IllegalStateException("acquire lock failed");
            }
        } finally {
            if (locked) {
                lockManager.remove(key);
            }
        }
    }

    private LockManager getLockManager(LockOperationConfig lockCfg, Method method, Class<?> targetClass) {
        LockConfigMetadata metadata = getLockConfigMetadata(lockCfg, method, targetClass);
        return metadata.lockManager;
    }

    private String generateLockKey(LockOperationConfig lockCfg, Method method, Class<?> targetClass, Object[] args) {
        if (StringUtils.hasText(lockCfg.getKey())) {
            return lockCfg.getKey();
        }
        LockConfigMetadata metadata = getLockConfigMetadata(lockCfg, method, targetClass);
        return metadata.keyGenerator.generate(targetClass, method, lockCfg.getPrefix(), args);
    }

    protected LockConfigMetadata getLockConfigMetadata(LockOperationConfig lockCfg, Method method, Class<?> targetClass) {
        LockConfigCacheKey cacheKey = new LockConfigCacheKey(lockCfg, method, targetClass);
        LockConfigMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            KeyGenerator keyGenerator;
            if (StringUtils.hasText(lockCfg.getKeyGenerator())) {
                keyGenerator = getBean(lockCfg.getKeyGenerator(), KeyGenerator.class);
            } else {
                keyGenerator = getKeyGenerator();
            }
            LockManager lockManager;
            if (StringUtils.hasText(lockCfg.getLockManager())) {
                lockManager = getBean(lockCfg.getLockManager(), LockManager.class);
            } else {
                lockManager = getLockManager();
            }
            metadata = new LockConfigMetadata(lockCfg, method, targetClass, keyGenerator, lockManager);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    /**
     * Return a bean with the specified name and type. Used to resolve services that
     * are referenced by name in a {@link LockOperationConfig}.
     * @param beanName the name of the bean, as defined by the operation
     * @param expectedType type for the bean
     * @return the bean matching that name
     * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException if such bean does not exist
     * @see LockOperationConfig#keyGenerator
     * @see LockOperationConfig#lockManager
     */
    protected <T> T getBean(String beanName, Class<T> expectedType) {
        return BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.beanFactory, expectedType, beanName);
    }

    protected abstract LockConfigSource getLockConfigSource();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.lockManager = getBean(SIMPLE_LOCK_MANAGER_BEAN_NAME, LockManager.class);
    }

    private Class<?> getTargetClass(Object target) {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
        if (targetClass == null && target != null) {
            targetClass = target.getClass();
        }
        return targetClass;
    }

    private static final class LockConfigCacheKey implements Comparable<LockConfigCacheKey> {

        private final LockOperationConfig lockCfg;

        private final AnnotatedElementKey methodCacheKey;

        private LockConfigCacheKey(LockOperationConfig lockCfg, Method method, Class<?> targetClass) {
            this.lockCfg = lockCfg;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LockConfigCacheKey)) {
                return false;
            }
            LockConfigCacheKey otherKey = (LockConfigCacheKey) other;
            return (this.lockCfg.equals(otherKey.lockCfg) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public int compareTo(LockConfigCacheKey other) {
            int result = this.lockCfg.getName().compareTo(other.lockCfg.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }

    protected static class LockConfigMetadata {

        private final LockOperationConfig lockCfg;

        private final Method method;

        private final Class<?> targetClass;

        private final KeyGenerator keyGenerator;

        private final LockManager lockManager;

        public LockConfigMetadata(LockOperationConfig lockCfg,
                                  Method method,
                                  Class<?> targetClass,
                                  KeyGenerator keyGenerator,
                                  LockManager lockManager) {
            this.lockCfg = lockCfg;
            this.method = method;
            this.targetClass = targetClass;
            this.keyGenerator = keyGenerator;
            this.lockManager = lockManager;
        }
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public LockManager getLockManager() {
        return lockManager;
    }
}
