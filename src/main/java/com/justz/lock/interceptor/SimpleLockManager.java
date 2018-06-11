package com.justz.lock.interceptor;

import com.justz.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Default lock manager. use {@link StringRedisTemplate} operate the lock key
 */
public class SimpleLockManager implements LockManager {

    private Logger logger = LoggerFactory.getLogger(SimpleLockManager.class);

    // 占用锁的最大时间 单位 秒
    private static final int LOCK_HOLD_MAX_SECONDS = 60;

    // 线程休息时间 单位 毫秒
    private static final int LOCK_RETRY_INTERVAL = 50;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleLockManager(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean lock(String key, long maxWaitMillSeconds) {
        long now = System.currentTimeMillis();
        while (System.currentTimeMillis() - now < maxWaitMillSeconds) {
            try {
                boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1");
                if (success) {
                    try {
                        stringRedisTemplate.expire(key, LOCK_HOLD_MAX_SECONDS, TimeUnit.SECONDS);
                        return true;
                    } catch (Exception exception) {
                        logger.debug("set lock timeout failed, lockKey: {}", key, exception);
                        try {
                            stringRedisTemplate.delete(key);
                        } catch (Exception ex) {
                            logger.debug("remove lock failed", ex);
                        }
                    }
                }
                TimeUnit.MILLISECONDS.sleep(LOCK_RETRY_INTERVAL);
            } catch (Exception exception) {
                logger.debug("acquire lock failed，lockKey: {}", key, exception);
            }
        }
        return false;
    }

    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }
}
