package com.zl.learn.threads.exception;

/**
 * 未找到配置的线程池时抛出该异常
 */
public class NoExecutorExecutor extends RuntimeException{
    public NoExecutorExecutor(String message) {
        super(message);
    }
}
