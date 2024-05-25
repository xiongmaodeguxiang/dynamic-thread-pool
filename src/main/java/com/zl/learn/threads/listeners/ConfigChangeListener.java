package com.zl.learn.threads.listeners;

import com.zl.learn.threads.events.ConfigChangeEvent;
import com.zl.learn.threads.events.MetadataEvent;
import com.zl.learn.threads.events.MetadataEvents;
import com.zl.learn.threads.executor.ExecutorInstances;
import com.zl.learn.threads.executor.ExecutorMetadata;
import com.zl.learn.threads.untils.ListUtils;
import com.zl.learn.threads.untils.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

public class ConfigChangeListener implements ApplicationListener<ConfigChangeEvent>, ApplicationEventPublisherAware {
    public static final String DYNAMIC_EXECUTOR_PREFIX = "dynamic.executors";
    private ApplicationEventPublisher publisher;
    @Autowired
    ExecutorInstances executorInstances;

    @Override
    public void onApplicationEvent(ConfigChangeEvent event) {
        String content = event.getContent();
        Yaml yaml = new Yaml();
        Map<String, Object> properties = yaml.load(content);
        List<ExecutorMetadata> newExecutorMetadata = PropertiesUtil.getList(DYNAMIC_EXECUTOR_PREFIX, properties, ExecutorMetadata.class);
        ExecutorInstances executorInstances = this.executorInstances;
        List<ExecutorMetadata> executorsMetadata = executorInstances.getExecutorsMetadata();
        List<MetadataEvent> metadataChangeEvents = ListUtils.getMetadataChangeEvents(executorsMetadata, newExecutorMetadata);
        publisher.publishEvent(new MetadataEvents(metadataChangeEvents));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
