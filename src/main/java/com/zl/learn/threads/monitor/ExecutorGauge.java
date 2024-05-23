package com.zl.learn.threads.monitor;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorGauge {
    private String executorName;
    private CollectorRegistry collectorRegistry;
    private String applicationName;
    private Gauge coreGauge;
    private Gauge maxGauge;
    private Gauge activeGauge;
    private Gauge poolGauge;
    private Gauge largestGauge;
    private Gauge queueGauge;

    public ExecutorGauge(String executorName, CollectorRegistry collectorRegistry, String applicationName){
        this.executorName = executorName;
        this.applicationName = applicationName;
        this.collectorRegistry = collectorRegistry;
        coreGauge = Gauge.build().name("core_size_monitor").labelNames("application", "executor_name")
                .help("core size monitor").register(collectorRegistry);
        maxGauge = Gauge.build().name("max_size_monitor").labelNames("application", "executor_name")
                .help("max size monitor").register(collectorRegistry);
        activeGauge = Gauge.build().name("active_size_monitor").labelNames("application", "executor_name")
                .help("active size monitor").register(collectorRegistry);
        poolGauge = Gauge.build().name("pool_size_monitor").labelNames("application", "executor_name")
                .help("pool size monitor").register(collectorRegistry);
        largestGauge = Gauge.build().name("largest_size_monitor").labelNames("application", "executor_name")
                .help("largest size monitor").register(collectorRegistry);
        queueGauge = Gauge.build().name("queue_size_monitor").labelNames("application", "executor_name")
                .help("queue size monitor").register(collectorRegistry);
    }

    public void gauge(ThreadPoolExecutor executor){
        gaugeCore(executor.getCorePoolSize());
        gaugeMax(executor.getMaximumPoolSize());
        gaugeActive(executor.getActiveCount());
        gaugePool(executor.getPoolSize());
        gaugeLargest(executor.getLargestPoolSize());
        gaugeQueue(executor.getQueue().size());
    }

    private void gaugeQueue(int size) {
        queueGauge.labels(applicationName, executorName).set(size);
    }

    private void gaugeLargest(int largestPoolSize) {
        largestGauge.labels(applicationName, executorName).set(largestPoolSize);
    }

    private void gaugePool(int poolSize) {
        poolGauge.labels(applicationName, executorName).set(poolSize);
    }

    private void gaugeActive(int activeCount) {
        activeGauge.labels(applicationName, executorName).set(activeCount);
    }

    private void gaugeMax(int maximumPoolSize) {
        maxGauge.labels(applicationName, executorName).set(maximumPoolSize);
    }

    private void gaugeCore(int corePoolSize) {
        coreGauge.labels(applicationName, executorName).set(corePoolSize);
    }

}
