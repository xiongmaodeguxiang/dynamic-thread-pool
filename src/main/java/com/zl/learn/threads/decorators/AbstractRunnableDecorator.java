package com.zl.learn.threads.decorators;


public abstract class AbstractRunnableDecorator implements Runnable {
    private Runnable task;

    public void run(){
        beforeRun();
        task.run();
    }

    protected abstract void beforeRun();

    public Runnable decorate(Runnable task){
        this.task = task;
        return doDecorate(task);
    }
    protected abstract Runnable doDecorate(Runnable task);
}
