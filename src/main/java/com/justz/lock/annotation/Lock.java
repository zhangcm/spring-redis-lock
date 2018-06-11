package com.justz.lock.annotation;

import com.justz.lock.enumeration.ActionType;

import java.lang.annotation.*;

/**
 * Annotation indicating that should set a lock before invoke method
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lock {

    String key() default "";

    String prefix() default "";

    long maxWaitMillSeconds() default 5000;

    String keyGenerator() default "";

    String lockManager() default "";

    ActionType actionAfterAcquireFailed() default ActionType.THROW_EXCEPTION;

}
