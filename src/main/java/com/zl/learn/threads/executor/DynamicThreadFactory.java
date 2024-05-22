package com.zl.learn.threads.executor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private String businessName;
    public DynamicThreadFactory (String businessName){
        this.businessName = businessName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName(businessName+"-pool-thread-"+ threadNumber.getAndIncrement());
        return t;
    }
}
