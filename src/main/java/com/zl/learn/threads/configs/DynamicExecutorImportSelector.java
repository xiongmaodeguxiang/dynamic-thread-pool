package com.zl.learn.threads.configs;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

public class DynamicExecutorImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{DynamicExecutorConfiguration.class.getName()};
    }
}
