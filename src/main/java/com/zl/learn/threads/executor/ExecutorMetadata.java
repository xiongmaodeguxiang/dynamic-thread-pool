package com.zl.learn.threads.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ExecutorMetadata {
    private String name;
    private Integer core;
    private Integer max;
    private Integer queueType;
    private Integer queueSize;
    private Long keepAliveTime;
    private Integer timeUnitType;
    private Integer handlerType;

}
