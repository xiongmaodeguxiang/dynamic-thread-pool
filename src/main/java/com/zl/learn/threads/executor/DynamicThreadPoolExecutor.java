package com.zl.learn.threads.executor;

import com.zl.learn.threads.queue.ReChangeBlockingQueue;
import com.zl.learn.threads.queue.ResizeBlockingQueue;

import java.util.concurrent.*;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {
    private ReChangeBlockingQueue reChangeBlockingQueue;
    public DynamicThreadPoolExecutor(
                                    String businessName,
                                    int corePoolSize,
                                     int maximumPoolSize,
                                     long keepAliveTime,
                                     TimeUnit unit,
                                    ReChangeBlockingQueue<Runnable> workQueue,
                                     RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new DynamicThreadFactory(businessName), handler);
        this.reChangeBlockingQueue = workQueue;
    }

    /**
     *
     * @param queueSize
     */
    public void resize(int queueSize){
        ReChangeBlockingQueue<Runnable> queue = (ReChangeBlockingQueue)getQueue();
        BlockingQueue<Runnable> delegate = queue.getDelegate();
        if(delegate instanceof ResizeBlockingQueue){
            ((ResizeBlockingQueue)delegate).resize(queueSize);
        }

    }

    /**
     * 调整queue队列的类型
     * @param queue
     */
    public void changeQueue(BlockingQueue<Runnable> queue){
        reChangeBlockingQueue.changeType(queue);
    }
}
