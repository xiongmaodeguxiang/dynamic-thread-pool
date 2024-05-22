package com.zl.learn.threads.annotations;

import com.zl.learn.threads.configs.DynamicExecutorImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(DynamicExecutorImportSelector.class)
public @interface EnableDynamicExecutor {
}
