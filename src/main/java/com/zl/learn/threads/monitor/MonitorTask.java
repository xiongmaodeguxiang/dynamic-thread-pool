package com.zl.learn.threads.monitor;

import com.zl.learn.threads.executor.DynamicThreadPoolExecutor;
import com.zl.learn.threads.executor.ExecutorInstances;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorTask implements Runnable {
    private PrometheusMeterRegistry meterRegistry;
    private CollectorRegistry prometheusRegistry;
    private ExecutorInstances executorInstances;
    private ExecutorGauge executorGauge;

    public MonitorTask(PrometheusMeterRegistry meterRegistry, ExecutorInstances executorInstances) {
        this.meterRegistry = meterRegistry;
        this.executorInstances = executorInstances;
        this.prometheusRegistry = meterRegistry.getPrometheusRegistry();
        List<DynamicThreadPoolExecutor> allExecutors = executorInstances.getAllExecutors();
        executorGauge = new ExecutorGauge(prometheusRegistry);

    }

    @Override
    public void run() {
        List<DynamicThreadPoolExecutor> allExecutors = executorInstances.getAllExecutors();
        for(DynamicThreadPoolExecutor executor : allExecutors) {
            executorGauge.gauge(executor);
        }
    }
}