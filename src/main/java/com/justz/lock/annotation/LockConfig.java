package com.justz.lock.annotation;

import java.lang.annotation.*;

/**
 * {@code @LockConfig} provides a mechanism for sharing common lock-related
 * settings at the class level
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockConfig {

    String keyGenerator() default "";

    String lockManager() default "";
}
