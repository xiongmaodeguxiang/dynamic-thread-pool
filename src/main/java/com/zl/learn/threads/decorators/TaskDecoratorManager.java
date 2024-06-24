package com.zl.learn.threads.decorators;

import java.util.List;
import java.util.concurrent.Callable;

public class TaskDecoratorManager {
    private List<AbstractCallableDecorator> callableDecorators;
    private List<AbstractRunnableDecorator> runnableDecorators;

    public TaskDecoratorManager(List<AbstractCallableDecorator> callableDecorators, List<AbstractRunnableDecorator> runnableDecorators){
        this.callableDecorators = callableDecorators;
        this.runnableDecorators = runnableDecorators;
    }

    public <T> Callable<T> decorate(Callable<T> callable){
        for (AbstractCallableDecorator callableDecorator : this.callableDecorators) {
            callable = callableDecorator.decorate(callable);
        }
        return callable;
    }

    public Runnable decorate(Runnable runnable){
        for (AbstractRunnableDecorator runnableDecorator : this.runnableDecorators) {
            runnable = runnableDecorator.decorate(runnable);
        }
        return runnable;
    }

}
