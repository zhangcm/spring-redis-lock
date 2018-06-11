package com.justz.lock.annotation;

import java.lang.annotation.*;

/**
 * Indicate the param will be used to generate lock key
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockParam {
}
