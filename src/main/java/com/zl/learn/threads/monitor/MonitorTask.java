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
    private String applicationName;
    private Map<String, ExecutorGauge> gaugeMap = new HashMap<String, ExecutorGauge>();

    public MonitorTask(PrometheusMeterRegistry meterRegistry, ExecutorInstances executorInstances,String applicationName) {
        this.meterRegistry = meterRegistry;
        this.executorInstances = executorInstances;
        this.prometheusRegistry = meterRegistry.getPrometheusRegistry();
        this.applicationName = applicationName;
        List<DynamicThreadPoolExecutor> allExecutors = executorInstances.getAllExecutors();
        for(DynamicThreadPoolExecutor executor : allExecutors){
            String businessName = executor.getBusinessName();
            gaugeMap.put(businessName, new ExecutorGauge(businessName, prometheusRegistry, applicationName));
        }

    }

    @Override
    public void run() {
        List<DynamicThreadPoolExecutor> allExecutors = executorInstances.getAllExecutors();
        for(DynamicThreadPoolExecutor executor : allExecutors) {
            ExecutorGauge gauge = gaugeMap.computeIfAbsent(executor.getBusinessName(), k -> new ExecutorGauge(executor.getBusinessName(), prometheusRegistry, applicationName));
            gauge.gauge(executor);
        }
    }
}