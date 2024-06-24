package com.zl.learn.threads.decorators;

import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;

public class SwRunnableDecorator extends AbstractRunnableDecorator{
    @Override
    protected void beforeRun() {

    }

    @Override
    protected Runnable doDecorate(Runnable task) {
        return RunnableWrapper.of(task);
    }
}
