package com.justz.lock.annotation;

import java.lang.annotation.*;
import com.justz.lock.interceptor.SimpleKeyGenerator;

/**
 * Indicate the param will be used to generate lock key
 *
 * @see SimpleKeyGenerator
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LockParam {
}
