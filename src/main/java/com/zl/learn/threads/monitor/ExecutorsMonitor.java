package com.zl.learn.threads.monitor;

import com.zl.learn.threads.executor.ExecutorInstances;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.springframework.context.SmartLifecycle;

import java.rmi.registry.Registry;
import java.util.concurrent.*;

/**
 * 用来对线程池进行监控
 */
public class ExecutorsMonitor implements SmartLifecycle {

    private ExecutorInstances executorInstances;
    private PrometheusMeterRegistry meterRegistry;
    private ScheduledExecutorService executorService;
    private String applicationName;
    public ExecutorsMonitor(ExecutorInstances executorInstances, PrometheusMeterRegistry meterRegistry,String applicationName){
        this.executorInstances = executorInstances;
        this.meterRegistry = meterRegistry;
        this.applicationName = applicationName;
        executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("executor-monitor-thread");
                return t;
            }
        });
    }

    @Override
    public void start() {
        executorService.scheduleAtFixedRate(new MonitorTask(meterRegistry,executorInstances, applicationName), 5, 2, TimeUnit.SECONDS);
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
