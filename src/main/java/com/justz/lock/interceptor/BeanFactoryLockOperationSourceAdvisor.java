package com.justz.lock.interceptor;

import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * Advisor driven by a {@link LockConfigSource}, used to include a
 * lock advice bean for methods that need lock.
 */
public class BeanFactoryLockOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private LockConfigSource lockConfigSource;

    private final Pointcut pointcut = new LockConfigSourcePointcut() {
        @Override
        protected LockConfigSource getLockConfigSource() {
            return lockConfigSource;
        }
    };

    /**
     * Set the lock operation attribute source which is used to find lock
     * attributes. This should usually be identical to the source reference
     * set on the lock interceptor itself.
     */
    public void setLockConfigSource(LockConfigSource lockConfigSource) {
        this.lockConfigSource = lockConfigSource;
    }


    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

}
