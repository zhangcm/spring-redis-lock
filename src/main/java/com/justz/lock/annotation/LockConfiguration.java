package com.justz.lock.annotation;

import com.justz.lock.LockManager;
import com.justz.lock.config.LockManagementConfigUtils;
import com.justz.lock.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.core.StringRedisTemplate;

import static com.justz.lock.config.LockManagementConfigUtils.SIMPLE_LOCK_MANAGER_BEAN_NAME;

/**
 * {@code @Configuration} class that registers the infrastructure beans necessary
 * to enable proxy-based annotation-driven lock management.
 */
@Configuration
public class LockConfiguration extends AbstractLockConfiguration {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Bean(name = LockManagementConfigUtils.LOCK_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLockOperationSourceAdvisor lockAdvisor() {
        BeanFactoryLockOperationSourceAdvisor advisor = new BeanFactoryLockOperationSourceAdvisor();
        advisor.setLockConfigSource(lockConfigSource());
        advisor.setAdvice(lockInterceptor());
        advisor.setOrder(this.enableLock.<Integer>getNumber("order"));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LockConfigSource lockConfigSource() {
        return new AnnotationLockConfigSource();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LockInterceptor lockInterceptor() {
        LockInterceptor interceptor = new LockInterceptor();
        interceptor.setLockConfigSource(lockConfigSource());
        return interceptor;
    }

    @Bean(name = SIMPLE_LOCK_MANAGER_BEAN_NAME)
    public LockManager simpleLockManager() {
        return new SimpleLockManager(stringRedisTemplate);
    }

}
