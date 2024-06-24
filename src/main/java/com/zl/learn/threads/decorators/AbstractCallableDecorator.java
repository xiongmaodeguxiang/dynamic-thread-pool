package com.zl.learn.threads.decorators;

import java.util.concurrent.Callable;

public abstract class AbstractCallableDecorator<T> implements Callable<T> {
    private Callable<T> task;

    @Override
    public T call() throws Exception {
        beforeCall(task);
        return task.call();
    }

    protected abstract void beforeCall(Callable<T> task);

    public Callable<T> decorate(Callable<T> task){
        this.task = task;
        return doDecorate(task);
    }
    protected abstract Callable<T> doDecorate(Callable<T> task);
}
