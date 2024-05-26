package com.zl.learn.threads.monitor;

import com.zl.learn.threads.executor.DynamicThreadPoolExecutor;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorGauge {
    private CollectorRegistry collectorRegistry;
    private Gauge coreGauge;
    private Gauge maxGauge;
    private Gauge activeGauge;
    private Gauge poolGauge;
    private Gauge largestGauge;
    private Gauge queueGauge;

    public ExecutorGauge(CollectorRegistry collectorRegistry){
        this.collectorRegistry = collectorRegistry;
        coreGauge = Gauge.build().name("core_size_monitor").labelNames("executor_name")
                .help("core size monitor").register(collectorRegistry);
        maxGauge = Gauge.build().name("max_size_monitor").labelNames( "executor_name")
                .help("max size monitor").register(collectorRegistry);
        activeGauge = Gauge.build().name("active_size_monitor").labelNames("executor_name")
                .help("active size monitor").register(collectorRegistry);
        poolGauge = Gauge.build().name("pool_size_monitor").labelNames("executor_name")
                .help("pool size monitor").register(collectorRegistry);
        largestGauge = Gauge.build().name("largest_size_monitor").labelNames("executor_name")
                .help("largest size monitor").register(collectorRegistry);
        queueGauge = Gauge.build().name("queue_size_monitor").labelNames("executor_name")
                .help("queue size monitor").register(collectorRegistry);
    }

    public void gauge(DynamicThreadPoolExecutor executor){
        String businessName = executor.getBusinessName();
        gaugeCore(businessName,executor.getCorePoolSize());
        gaugeMax(businessName,executor.getMaximumPoolSize());
        gaugeActive(businessName,executor.getActiveCount());
        gaugePool(businessName,executor.getPoolSize());
        gaugeLargest(businessName,executor.getLargestPoolSize());
        gaugeQueue(businessName,executor.getQueue().size());
    }

    private void gaugeQueue(String executorName, int size) {
        queueGauge.labels(executorName).set(size);
    }

    private void gaugeLargest(String executorName, int largestPoolSize) {
        largestGauge.labels(executorName).set(largestPoolSize);
    }

    private void gaugePool(String executorName, int poolSize) {
        poolGauge.labels(executorName).set(poolSize);
    }

    private void gaugeActive(String executorName, int activeCount) {
        activeGauge.labels(executorName).set(activeCount);
    }

    private void gaugeMax(String executorName, int maximumPoolSize) {
        maxGauge.labels(executorName).set(maximumPoolSize);
    }

    private void gaugeCore(String executorName, int corePoolSize) {
        coreGauge.labels(executorName).set(corePoolSize);
    }

}
