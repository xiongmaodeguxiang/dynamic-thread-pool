package com.zl.learn.threads.listeners;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.zl.learn.threads.events.ConfigChangeEvent;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.context.*;

import java.util.Map;

public class NacosConfigMetadataEventListener implements ApplicationListener<NacosConfigMetadataEvent>, ApplicationContextAware, ApplicationEventPublisherAware {
    private ConfigServiceBeanBuilder configServiceBeanBuilder;
    private ApplicationEventPublisher publisher;
    @Override
    public void onApplicationEvent(NacosConfigMetadataEvent event) {
        String groupId = event.getGroupId();
        String dataId = event.getDataId();
        Map<String, Object> nacosProperties = event.getNacosPropertiesAttributes();
        ConfigService configService= configServiceBeanBuilder.build(nacosProperties);
        String config = null;
        try {
            config = configService.getConfig(dataId, groupId, 10000);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        publisher.publishEvent(new ConfigChangeEvent(config));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        configServiceBeanBuilder = applicationContext.getBean(ConfigServiceBeanBuilder.BEAN_NAME,
                ConfigServiceBeanBuilder.class);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }
}
