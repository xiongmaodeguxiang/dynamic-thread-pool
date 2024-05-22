package com.zl.learn.threads.configs;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.spring.context.annotation.EnableNacos;
import com.zl.learn.threads.events.MetadataEvent;
import com.zl.learn.threads.events.MetadataEvents;
import com.zl.learn.threads.executor.ExecutorInstances;
import com.zl.learn.threads.executor.ExecutorMetadata;
import com.zl.learn.threads.untils.ListUtils;
import com.zl.learn.threads.untils.PropertiesUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

@Configuration
//@EnableNacos(globalProperties = @NacosProperties(serverAddr = "${nacos.server}"))
@EnableNacos(globalProperties = @NacosProperties())
public class DynamicExecutorConfiguration implements ApplicationEventPublisherAware {
    public static final String DYNAMIC_EXECUTOR_PREFIX = "dynamic.executors";

    ApplicationEventPublisher publisher;


    @Bean
    ExecutorInstances executorInstances(){
        return new ExecutorInstances();
    }

    @NacosConfigListener(dataId = "dynamic-executor.yaml", groupId = "EXECUTOR")
    public void onReceive(String content){
        Yaml yaml = new Yaml();
        Map<String, Object> properties = yaml.load(content);
        List<ExecutorMetadata> newExecutorMetadata = PropertiesUtil.getList(DYNAMIC_EXECUTOR_PREFIX, properties, ExecutorMetadata.class);
        ExecutorInstances executorInstances = executorInstances();
        List<ExecutorMetadata> executorsMetadata = executorInstances.getExecutorsMetadata();
        List<MetadataEvent> metadataChangeEvents = ListUtils.getMetadataChangeEvents(executorsMetadata, newExecutorMetadata);
        publisher.publishEvent(new MetadataEvents(metadataChangeEvents));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }
}
