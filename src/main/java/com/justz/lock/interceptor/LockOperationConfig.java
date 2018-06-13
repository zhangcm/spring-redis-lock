package com.justz.lock.interceptor;

import com.justz.lock.enumeration.ActionType;

import java.util.concurrent.TimeUnit;

/**
 * config for lock operation
 */
public class LockOperationConfig {

    private String name;

    private String key;

    private String prefix;

    private String keyGenerator;

    private long maxWaitMillSeconds;

    private String lockManager;

    private ActionType actionAfterAcquireFailed;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(String keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public long getMaxWaitMillSeconds() {
        return maxWaitMillSeconds;
    }

    public void setMaxWaitMillSeconds(long maxWaitMillSeconds) {
        this.maxWaitMillSeconds = maxWaitMillSeconds;
    }

    public String getLockManager() {
        return lockManager;
    }

    public void setLockManager(String lockManager) {
        this.lockManager = lockManager;
    }

    public ActionType getActionAfterAcquireFailed() {
        return actionAfterAcquireFailed;
    }

    public void setActionAfterAcquireFailed(ActionType actionAfterAcquireFailed) {
        this.actionAfterAcquireFailed = actionAfterAcquireFailed;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof LockOperationConfig && toString().equals(other.toString()));
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
