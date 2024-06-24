package com.zl.learn.threads.executor;

import com.zl.learn.threads.decorators.TaskDecoratorManager;
import com.zl.learn.threads.queue.ReChangeBlockingQueue;
import com.zl.learn.threads.queue.ResizeBlockingQueue;
import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;

import java.util.concurrent.*;

public class DynamicThreadPoolExecutor extends ThreadPoolExecutor {
    private ReChangeBlockingQueue reChangeBlockingQueue;
    private String businessName;
    private TaskDecoratorManager taskDecoratorManager;
    public DynamicThreadPoolExecutor(
                                    String businessName,
                                    int corePoolSize,
                                    int maximumPoolSize,
                                    long keepAliveTime,
                                    TimeUnit unit,
                                    ReChangeBlockingQueue<Runnable> workQueue,
                                    RejectedExecutionHandler handler,
                                    TaskDecoratorManager taskDecoratorManager) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new DynamicThreadFactory(businessName), handler);
        this.reChangeBlockingQueue = workQueue;
        this.businessName = businessName;
        this.taskDecoratorManager = taskDecoratorManager;
    }

    /**
     * 添加skywalking监控
     * @param command the task to execute
     */
    @Override
    public void execute(Runnable command) {
        super.execute(taskDecoratorManager.decorate(command));
    }

    /**
     * 添加skywalking监控
     * @param task the task to submit
     * @return
     * @param <T>
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return super.submit(taskDecoratorManager.decorate(task));
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

    /**
     * 获取线程池的名称
     * @return
     */
    public String getBusinessName() {
        return businessName;
    }
}
