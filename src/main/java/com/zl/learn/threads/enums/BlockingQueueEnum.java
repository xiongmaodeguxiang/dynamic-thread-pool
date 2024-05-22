package com.zl.learn.threads.enums;

import com.zl.learn.threads.queue.ResizeBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

@AllArgsConstructor
@Getter
public enum BlockingQueueEnum {
    ARRAY_BLOCKING(1, ArrayBlockingQueue.class),
    LINKED_BLOCKING(2, LinkedBlockingQueue.class),
    SYNCHRONOUS(3, SynchronousQueue.class),
    RESIZE_BLOCKING(4, ResizeBlockingQueue.class);

    private Integer type;
    private Class<?> kClass;

    public static BlockingQueue<Runnable> getQueue(int type, int size) {
        switch (type){
            case 1:
                return new ArrayBlockingQueue<Runnable>(size);
            case 2:
                if(size == -1){
                    return new LinkedBlockingQueue<Runnable>();
                }else{
                    return new LinkedBlockingQueue<Runnable>(size);
                }
            case 3:
                return new SynchronousQueue<Runnable>();
            case 4:
                return new ResizeBlockingQueue<Runnable>(size);
            default:
                return new ArrayBlockingQueue<Runnable>(size);
        }
    }

    public static BlockingQueue<Runnable> getDefault() {
        return new LinkedBlockingQueue<>();
    }
}
