package com.zl.learn.threads.decorators;

import org.apache.skywalking.apm.toolkit.trace.CallableWrapper;

import java.util.concurrent.Callable;

public class SwCallableDecorator<T> extends AbstractCallableDecorator<T>{
    @Override
    protected void beforeCall(Callable<T> task) {

    }

    @Override
    protected Callable<T> doDecorate(Callable<T> task) {
        return CallableWrapper.of(task);
    }
}
