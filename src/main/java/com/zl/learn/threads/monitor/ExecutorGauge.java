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
        coreGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
                .help("core size monitor").register(collectorRegistry);
        maxGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
                .help("max size monitor").register(collectorRegistry);
        activeGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
                .help("active size monitor").register(collectorRegistry);
        poolGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
                .help("pool size monitor").register(collectorRegistry);
        largestGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
                .help("largest size monitor").register(collectorRegistry);
        queueGauge = Gauge.build().name("executor_monitor").labelNames("application", "executor_name","type")
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
        queueGauge.labels(applicationName, executorName,"queue_size").set(size);
    }

    private void gaugeLargest(int largestPoolSize) {
        largestGauge.labels(applicationName, executorName,"largest_size").set(largestPoolSize);
    }

    private void gaugePool(int poolSize) {
        poolGauge.labels(applicationName, executorName,"pool_size").set(poolSize);
    }

    private void gaugeActive(int activeCount) {
        activeGauge.labels(applicationName, executorName,"active_size").set(activeCount);
    }

    private void gaugeMax(int maximumPoolSize) {
        maxGauge.labels(applicationName, executorName,"max_size").set(maximumPoolSize);
    }

    private void gaugeCore(int corePoolSize) {
        coreGauge.labels(applicationName, executorName,"core_size").set(corePoolSize);
    }

}
