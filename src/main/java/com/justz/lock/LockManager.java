package com.justz.lock;

/**
 * operation lock key
 */
public interface LockManager {

    boolean lock(String key, long maxWaitSecond);

    void remove(String key);
}
