package com.justz.lock.annotation;

import com.justz.lock.LockManager;

/**
 * Interface to be implemented by @{@link org.springframework.context.annotation.Configuration
 * Configuration} classes annotated with @{@link EnableLock} that wish or need to
 * specify explicitly how lock key are operated for annotation-driven lock management.
 */
public interface LockConfigurer {

    LockManager lockManager();
}
