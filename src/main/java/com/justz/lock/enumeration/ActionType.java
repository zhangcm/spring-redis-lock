package com.justz.lock.enumeration;

/**
 * Action type after acquire lock failed
 */
public enum ActionType {

    EXECUTE,
    THROW_EXCEPTION,
    RETURN_NULL
}
