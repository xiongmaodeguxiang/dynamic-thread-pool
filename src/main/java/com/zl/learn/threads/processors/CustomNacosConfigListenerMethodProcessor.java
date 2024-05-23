package com.zl.learn.threads.processors;

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.annotation.NacosConfigListener;
import com.alibaba.nacos.api.config.convert.NacosConfigConverter;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.spring.beans.factory.annotation.ConfigServiceBeanBuilder;
import com.alibaba.nacos.spring.context.annotation.config.NacosConfigListenerMethodProcessor;
import com.alibaba.nacos.spring.context.event.AnnotationListenerMethodProcessor;
import com.alibaba.nacos.spring.context.event.config.NacosConfigMetadataEvent;
import com.alibaba.nacos.spring.context.event.config.TimeoutNacosConfigListener;
import com.alibaba.nacos.spring.convert.converter.config.DefaultNacosConfigConverter;
import com.alibaba.nacos.spring.factory.NacosServiceFactory;
import com.alibaba.nacos.spring.util.PropertiesPlaceholderResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import static com.alibaba.nacos.spring.util.GlobalNacosPropertiesSource.CONFIG;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getConfigServiceBeanBuilder;
import static com.alibaba.nacos.spring.util.NacosBeanUtils.getNacosServiceFactoryBean;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes;

public class CustomNacosConfigListenerMethodProcessor extends AnnotationListenerMethodProcessor<NacosConfigListener>
        implements ApplicationContextAware, ApplicationEventPublisherAware, EnvironmentAware{
    /**
     * The bean name of {@link NacosConfigListenerMethodProcessor}
     */
    public static final String BEAN_NAME = "nacosConfigListenerMethodProcessor";

    /**
     * The bean name of {@link ConversionService} for Nacos Configuration
     */
    public static final String NACOS_CONFIG_CONVERSION_SERVICE_BEAN_NAME = "nacosConfigConversionService";

    private Properties globalNacosProperties;

    private NacosServiceFactory nacosServiceFactory;

    private ConversionService conversionService;

    private ConfigServiceBeanBuilder configServiceBeanBuilder;

    private Environment environment;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    protected void processListenerMethod(String beanName, final Object bean, Class<?> beanClass,
                                         final NacosConfigListener listener, final Method method,
                                         ApplicationContext applicationContext) {

        String dataId = listener.dataId();
        String groupId = listener.groupId();
        dataId = environment.resolvePlaceholders(dataId);
        groupId = environment.resolvePlaceholders(groupId);
        long timeout = listener.timeout();

        Assert.isTrue(StringUtils.hasText(dataId), "dataId must have content");
        Assert.isTrue(StringUtils.hasText(groupId), "groupId must have content");
        Assert.isTrue(timeout > 0, "timeout must be greater than zero");

        ConfigService configService = configServiceBeanBuilder.build(listener.properties());

        try {

            configService.addListener(dataId, groupId, new TimeoutNacosConfigListener(dataId, groupId, timeout) {

                @Override
                protected void onReceived(String config) {
                    Class<?> targetType = method.getParameterTypes()[0];
                    NacosConfigConverter configConverter = determineNacosConfigConverter(targetType, listener);
                    Object parameterValue = configConverter.convert(config);
                    // Execute target method
                    ReflectionUtils.invokeMethod(method, bean, parameterValue);
                }
            });
        } catch (NacosException e) {
            if (logger.isErrorEnabled()) {
                logger.error("ConfigService can't add Listener for dataId : " + dataId + " , groupId : " + groupId, e);
            }
        }

        publishMetadataEvent(beanName, bean, beanClass, dataId, groupId, listener, method);

    }

    private void publishMetadataEvent(String beanName, Object bean, Class<?> beanClass, String dataId, String groupId,
                                      NacosConfigListener listener, Method method) {

        NacosProperties nacosProperties = listener.properties();

        Properties resolvedNacosProperties = configServiceBeanBuilder.resolveProperties(nacosProperties);

        NacosConfigMetadataEvent metadataEvent = new NacosConfigMetadataEvent(listener);

        // Nacos Metadata
        metadataEvent.setDataId(dataId);
        metadataEvent.setGroupId(groupId);

        Map<String, Object> nacosPropertiesAttributes = getAnnotationAttributes(nacosProperties);
        metadataEvent.setNacosPropertiesAttributes(nacosPropertiesAttributes);
        metadataEvent.setNacosProperties(resolvedNacosProperties);

        // Bean Metadata
        metadataEvent.setBeanName(beanName);
        metadataEvent.setBean(bean);
        metadataEvent.setBeanType(beanClass);
        metadataEvent.setAnnotatedElement(method);

        // Publish event
        applicationEventPublisher.publishEvent(metadataEvent);
    }

    private ConfigService resolveConfigService(Properties nacosProperties, ApplicationContext applicationContext)
            throws BeansException {

        ConfigService configService = null;

        try {
            configService = nacosServiceFactory.createConfigService(nacosProperties);
        } catch (NacosException e) {
            throw new BeanCreationException(e.getErrMsg(), e);
        }

        return configService;
    }

    @Override
    protected boolean isCandidateMethod(Object bean, Class<?> beanClass, NacosConfigListener listener, Method method,
                                        ApplicationContext applicationContext) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length != 1) { // Only one argument on method
            if (logger.isWarnEnabled()) {
                logger.warn("Listener method [" + method + "] parameters' count must be one !");
            }
            return false;
        }

        Class<?> targetType = parameterTypes[0];

        NacosConfigConverter configConverter = determineNacosConfigConverter(targetType, listener);

        if (!configConverter.canConvert(targetType)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Listener method [" + method + "] is not a candidate , thus its parameter type ["
                        + targetType + "] can't be converted , please check NacosConfigConverter implementation : "
                        + configConverter.getClass().getName());
            }
        }

        return true;
    }

    private NacosConfigConverter determineNacosConfigConverter(Class<?> targetType, NacosConfigListener listener) {

        Class<?> converterClass = listener.converter();

        NacosConfigConverter configConverter = null;

        if (NacosConfigConverter.class.equals(converterClass)) { // Use default implementation

            configConverter = new DefaultNacosConfigConverter(targetType, conversionService);

        } else { // Use customized implementation

            configConverter = (NacosConfigConverter) instantiateClass(converterClass);

        }

        return configConverter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        globalNacosProperties = CONFIG.getMergedGlobalProperties(applicationContext);
        nacosServiceFactory = getNacosServiceFactoryBean(applicationContext);
        conversionService = determineConversionService(applicationContext);
        configServiceBeanBuilder = getConfigServiceBeanBuilder(applicationContext);
    }

    private ConversionService determineConversionService(ApplicationContext applicationContext) {

        String beanName = NACOS_CONFIG_CONVERSION_SERVICE_BEAN_NAME;

        ConversionService conversionService = applicationContext.containsBean(beanName) ?
                applicationContext.getBean(beanName, ConversionService.class) : null;

        if (conversionService == null) {
            conversionService = new DefaultFormattingConversionService();
        }

        return conversionService;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
