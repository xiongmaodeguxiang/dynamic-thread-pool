package com.zl.learn.threads.processors;

import com.alibaba.nacos.spring.context.annotation.config.NacosConfigListenerMethodProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

public class ChangeBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if(BeanDefinitionRegistry.class.isAssignableFrom(beanFactory.getClass())){
            BeanDefinitionRegistry registry = BeanDefinitionRegistry.class.cast(beanFactory);
            if(registry.containsBeanDefinition(NacosConfigListenerMethodProcessor.BEAN_NAME)){
                registry.removeBeanDefinition(NacosConfigListenerMethodProcessor.BEAN_NAME);
                BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(CustomNacosConfigListenerMethodProcessor.class);
                // ROLE_INFRASTRUCTURE
                beanDefinitionBuilder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                // Register
                registry.registerBeanDefinition(NacosConfigListenerMethodProcessor.BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
            }
        }

    }
}
