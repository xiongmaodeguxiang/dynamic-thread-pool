package com.zl.learn.threads.executor;

import com.zl.learn.threads.enums.BlockingQueueEnum;
import com.zl.learn.threads.enums.HandlerTypeEnum;
import com.zl.learn.threads.enums.TimeUnitEnum;
import com.zl.learn.threads.events.*;
import com.zl.learn.threads.exception.NoExecutorExecutor;
import com.zl.learn.threads.queue.ReChangeBlockingQueue;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 用于获取线程池
 */
public class ExecutorInstances implements ApplicationListener<MetadataEvents> {

    private ConcurrentHashMap<String, DynamicThreadPoolExecutor> executorMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ExecutorMetadata> metadata = new ConcurrentHashMap<>();

    public ThreadPoolExecutor getExecutor(String name){
        DynamicThreadPoolExecutor executor = executorMap.computeIfAbsent(name, k ->createDynamicThreadPoolExecutor(k,metadata.get(k)));
        if(Objects.isNull(executor)){
            throw new NoExecutorExecutor("no executor find for " + name);
        }
        return executor;
    }

    private DynamicThreadPoolExecutor createDynamicThreadPoolExecutor(String businessName, ExecutorMetadata executorMetadata) {
        if(Objects.isNull(executorMetadata)){
            throw new NoExecutorExecutor("no executor find for " + businessName);
        }
        Integer corePoolSize = executorMetadata.getCore();
        Integer maximumPoolSize = executorMetadata.getMax();
        Long keepAliveTime = executorMetadata.getKeepAliveTime();
        Integer blockingQueueType = executorMetadata.getQueueType();
        Integer queueSize = executorMetadata.getQueueSize();
        Integer TimeUnitType = executorMetadata.getTimeUnitType();
        Integer handlerType = executorMetadata.getHandlerType();
        if(StringUtils.isEmpty(businessName)){
            businessName = "default";
        }
        if(null == corePoolSize){
            corePoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        }
        if(null == maximumPoolSize){
            maximumPoolSize = corePoolSize * 2;
        }
        if(null == keepAliveTime){
            keepAliveTime = 0L;
        }
        TimeUnit timeUnit ;
        if(null == TimeUnitType){
            timeUnit = TimeUnit.SECONDS;
        }else{
            timeUnit = TimeUnitEnum.getTimeUnit(TimeUnitType);
        }
        BlockingQueue<Runnable> blockingQueue;
        if(null == blockingQueueType){
            blockingQueue = BlockingQueueEnum.getDefault();
        }else{
            blockingQueue = BlockingQueueEnum.getQueue(blockingQueueType, queueSize);
        }
        RejectedExecutionHandler handler;
        if(null != handlerType){
            handler = HandlerTypeEnum.getHandlerClass(handlerType);
        }else{
            handler = HandlerTypeEnum.getDefault();
        }
        return new DynamicThreadPoolExecutor(businessName, corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, new ReChangeBlockingQueue<>(blockingQueue), handler);
    }

    public List<ExecutorMetadata> getExecutorsMetadata(){
        return new ArrayList<>(metadata.values());
    }

    @Override
    public void onApplicationEvent(MetadataEvents event) {
        List<MetadataEvent> source = (List<MetadataEvent>) event.getSource();
        if(CollectionUtils.isEmpty(source)){
            return;
        }
        for (MetadataEvent metadataEvent : source) {
            dealEvent(metadataEvent);
        }
    }

    private void dealEvent(MetadataEvent metadataEvent) {
        if(metadataEvent instanceof MetadataAddEvent){
            dealAddEvent((MetadataAddEvent)metadataEvent);
        }else if(metadataEvent instanceof MetadataDeleteEvent){
            dealDeleteEvent((MetadataDeleteEvent)metadataEvent);
        }else if(metadataEvent instanceof MetadataChangeEvent){
            dealChangeEvent((MetadataChangeEvent)metadataEvent);
        }
    }

    private void dealChangeEvent(MetadataChangeEvent metadataEvent) {
        ExecutorMetadata oldMetadata = metadataEvent.getOldMetadata();
        ExecutorMetadata newMetadata = metadataEvent.getNewMetadata();
        metadata.put(newMetadata.getName(), newMetadata);
        DynamicThreadPoolExecutor executor = executorMap.get(newMetadata.getName());
        if(Objects.isNull(executor)){
            throw new NoExecutorExecutor("no executor found for " + newMetadata.getName());
        }
        if(!Objects.equals(oldMetadata.getCore(), newMetadata.getCore())){
            executor.setCorePoolSize(newMetadata.getCore());
        }
        if(!Objects.equals(oldMetadata.getMax(), newMetadata.getMax())){
            executor.setMaximumPoolSize(newMetadata.getMax());
        }
        if(!Objects.equals(oldMetadata.getKeepAliveTime(), newMetadata.getKeepAliveTime()) ||
                !Objects.equals(oldMetadata.getTimeUnitType(), newMetadata.getTimeUnitType()) ){
            executor.setKeepAliveTime(newMetadata.getKeepAliveTime(),TimeUnitEnum.getTimeUnit(newMetadata.getTimeUnitType()));
        }
        if(!Objects.equals(oldMetadata.getQueueType(), newMetadata.getQueueType())){
            executor.changeQueue(BlockingQueueEnum.getQueue(newMetadata.getQueueType(),newMetadata.getQueueSize()));
        }

        if(!Objects.equals(oldMetadata.getQueueType(), newMetadata.getQueueType()) ||
            !Objects.equals(oldMetadata.getQueueSize(), newMetadata.getQueueSize())){
            executor.resize( newMetadata.getQueueSize());
        }

        if(!Objects.equals(oldMetadata.getHandlerType(), newMetadata.getHandlerType())){
            executor.setRejectedExecutionHandler(HandlerTypeEnum.getHandlerClass(newMetadata.getHandlerType()));
        }
    }

    private void dealDeleteEvent(MetadataDeleteEvent metadataEvent) {
        metadata.remove(metadataEvent.getMetadata().getName());
        executorMap.remove(metadataEvent.getMetadata().getName());
    }

    private void dealAddEvent(MetadataAddEvent metadataEvent) {
        metadata.put(metadataEvent.getMetadata().getName(), metadataEvent.getMetadata());
        executorMap.put(metadataEvent.getMetadata().getName(), createDynamicThreadPoolExecutor(metadataEvent.getMetadata().getName(), metadataEvent.getMetadata()));
    }
}
