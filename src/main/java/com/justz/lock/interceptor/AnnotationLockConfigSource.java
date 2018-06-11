package com.justz.lock.interceptor;

import com.justz.lock.annotation.LockAnnotationParser;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of the {@link LockConfigSource} interface for working with lock metadata in annotation format.
 */
public class AnnotationLockConfigSource extends AbstractFallbackLockConfigSource implements Serializable {

    private final Set<LockAnnotationParser> annotationParsers;

    public AnnotationLockConfigSource() {
        this.annotationParsers = new LinkedHashSet<>(1);
        this.annotationParsers.add(new SpringLockAnnotationParser());
    }

    @Override
    protected Collection<LockOperationConfig> findLockConfig(final Method method) {
        return determineCacheOperations(new LockOperationConfigProvider() {
            @Override
            public Collection<LockOperationConfig> getLockOperationConfigs(LockAnnotationParser parser) {
                return parser.parseLockAnnotations(method);
            }
        });
    }

    @Override
    protected Collection<LockOperationConfig> findLockConfig(final Class<?> clazz) {
        return determineCacheOperations(new LockOperationConfigProvider() {
            @Override
            public Collection<LockOperationConfig> getLockOperationConfigs(LockAnnotationParser parser) {
                return parser.parseLockAnnotations(clazz);
            }
        });
    }

    /**
     * Determine the lock operation(s) for the given {@link LockOperationConfigProvider}.
     * <p>This implementation delegates to configured
     * {@link LockOperationConfigProvider}s for parsing known annotations into
     *  metadata attribute class.
     * <p>Can be overridden to support custom annotations that carry
     * lock metadata.
     * @param provider the lock operation provider to use
     * @return the configured lock operations, or {@code null} if none found
     */
    protected Collection<LockOperationConfig> determineCacheOperations(LockOperationConfigProvider provider) {
        Collection<LockOperationConfig> cfgs = null;
        for (LockAnnotationParser annotationParser : this.annotationParsers) {
            Collection<LockOperationConfig> annCfgs = provider.getLockOperationConfigs(annotationParser);
            if (annCfgs != null) {
                if (cfgs == null) {
                    cfgs = new ArrayList<LockOperationConfig>();
                }
                cfgs.addAll(annCfgs);
            }
        }
        return cfgs;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationLockConfigSource)) {
            return false;
        }
        AnnotationLockConfigSource otherCos = (AnnotationLockConfigSource) other;
        return this.annotationParsers.equals(otherCos.annotationParsers);
    }

    @Override
    public int hashCode() {
        return this.annotationParsers.hashCode();
    }

    /**
     * Callback interface providing {@link LockOperationConfig} instance(s) based on
     * a given {@link LockAnnotationParser}.
     */
    protected interface LockOperationConfigProvider {

        /**
         * Return the {@link LockOperationConfig} instance(s) provided by the specified parser.
         * @param parser the parser to use
         * @return the lock configs, or {@code null} if none found
         */
        Collection<LockOperationConfig> getLockOperationConfigs(LockAnnotationParser parser);
    }
}
