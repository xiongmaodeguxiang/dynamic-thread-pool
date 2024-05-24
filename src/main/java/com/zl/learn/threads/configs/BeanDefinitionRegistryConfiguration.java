package com.zl.learn.threads.configs;

import com.zl.learn.threads.processors.ChangeDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanDefinitionRegistryConfiguration {
    @Bean
    ChangeDefinitionRegistryPostProcessor changeDefinitionBeanFactorProcessor(){
        return new ChangeDefinitionRegistryPostProcessor();
    }
}
