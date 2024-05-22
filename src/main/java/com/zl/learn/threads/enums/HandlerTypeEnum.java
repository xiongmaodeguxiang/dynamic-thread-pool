package com.zl.learn.threads.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@AllArgsConstructor
@Getter
public enum HandlerTypeEnum {
    ABORT(1, ThreadPoolExecutor.AbortPolicy.class),
    CALLER(2, ThreadPoolExecutor.CallerRunsPolicy.class),
    DISCARD_OLDEST(3, ThreadPoolExecutor.DiscardOldestPolicy.class),
    DISCARD(4, ThreadPoolExecutor.DiscardPolicy.class);

    private int type;
    private Class<? extends RejectedExecutionHandler> handlerClass;

    public static RejectedExecutionHandler getHandlerClass(int type){
        for (HandlerTypeEnum handlerTypeEnum : HandlerTypeEnum.values()) {
            if (handlerTypeEnum.getType() == type) {
                try {
                    return handlerTypeEnum.getHandlerClass().newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ThreadPoolExecutor.DiscardPolicy();
    }
    public static RejectedExecutionHandler getDefault(){
        return new ThreadPoolExecutor.DiscardPolicy();
    }
}
