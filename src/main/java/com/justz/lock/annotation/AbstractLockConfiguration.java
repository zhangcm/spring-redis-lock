package com.justz.lock.annotation;

import com.justz.lock.LockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * Abstract base {@code @Configuration} class providing common structure
 * for enabling annotation-driven lock management capability.
 */
@Configuration
public class AbstractLockConfiguration implements ImportAware {

    protected AnnotationAttributes enableLock;

    protected LockManager lockManager;


    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLock = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLock.class.getName(), false));
        if (this.enableLock == null) {
            throw new IllegalArgumentException(
                    "@EnableLock is not present on importing class " + importMetadata.getClassName());
        }
    }
    
    @Autowired(required = false)
    void setCongigurers(Collection<LockConfigurer> configurers) {
        if (CollectionUtils.isEmpty(configurers)) {
            return;
        }
        if (configurers.size() > 1) {
            throw new IllegalStateException("Only one LockConfigurer may exist");
        }
        LockConfigurer configurer = configurers.iterator().next();
        this.lockManager = configurer.lockManager();
    }
}
