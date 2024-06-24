package com.zl.learn.threads.configs;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.zl.learn.threads.decorators.*;
import com.zl.learn.threads.events.ConfigChangeEvent;
import com.zl.learn.threads.executor.ExecutorInstances;
import com.zl.learn.threads.listeners.ConfigChangeListener;
import com.zl.learn.threads.listeners.NacosConfigMetadataEventListener;
import com.zl.learn.threads.monitor.ExecutorsMonitor;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableNacos(globalProperties = @NacosProperties())
public class DynamicExecutorConfiguration implements ApplicationEventPublisherAware {


    ApplicationEventPublisher publisher;

    @Bean
    TaskDecoratorManager taskDecoratorManager(ObjectProvider<List<AbstractCallableDecorator>> callableProvider, ObjectProvider<List<AbstractRunnableDecorator>> runnableProvider){
        return new TaskDecoratorManager(callableProvider.getIfAvailable(()-> new ArrayList<>()),runnableProvider.getIfAvailable(()-> new ArrayList<>()));
    }
    @Bean
    SwRunnableDecorator swRunnableDecorator(){
        return new SwRunnableDecorator();
    }

    @Bean
    SwCallableDecorator swCallableDecorator(){
        return new SwCallableDecorator();
    }
    @Bean
    NacosConfigMetadataEventListener nacosConfigMetadataEventListener(){
        return new NacosConfigMetadataEventListener();
    }

    @Bean
    ConfigChangeListener configChangeListener(){
        return new ConfigChangeListener();
    }

    @Bean
    ExecutorInstances executorInstances(){
        return new ExecutorInstances();
    }
    @Bean
    ExecutorsMonitor executorsMonitor(ExecutorInstances executorInstances, PrometheusMeterRegistry meterRegistry){
        return new ExecutorsMonitor(executorInstances, meterRegistry);
    }

//    @Bean
//    public MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(){
//        return registry -> registry.config().commonTags("application", applicationName);
//    }

    @NacosConfigListener(dataId = "${spring.application.name}-executor.yaml", groupId = "EXECUTOR")
    public void onReceive(String content){
        publisher.publishEvent(new ConfigChangeEvent(content));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }
}
