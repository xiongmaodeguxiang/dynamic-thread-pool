package com.zl.learn.threads.queue;

import java.util.concurrent.BlockingQueue;

public interface ChangeQueue<T> {
    void changeType(BlockingQueue<T> t);
    boolean supportResize();
}
