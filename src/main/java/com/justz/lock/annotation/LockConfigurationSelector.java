package com.justz.lock.annotation;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects which implementation of {@link AbstractLockConfiguration} should be used
 * based on the value of {@link EnableLock#mode} on the importing {@code @Configuration}
 * class.
 */
public class LockConfigurationSelector extends AdviceModeImportSelector<EnableLock> {

    protected String[] selectImports(AdviceMode adviceMode) {
        List<String> result = new ArrayList<String>();
        result.add(LockConfiguration.class.getName());
        return result.toArray(new String[result.size()]);
    }

}
