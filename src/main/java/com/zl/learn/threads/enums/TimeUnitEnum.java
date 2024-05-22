package com.zl.learn.threads.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Getter
public enum  TimeUnitEnum {
    SECONDS(1, TimeUnit.SECONDS),
    MILLISECONDS(2, TimeUnit.MICROSECONDS);

    private Integer type;
    private TimeUnit timeUnit;

    public static TimeUnit getTimeUnit(Integer type){
        if(null == type){
            return TimeUnit.SECONDS;
        }
        for (TimeUnitEnum value : TimeUnitEnum.values()) {
            if (value.getType().intValue() == type) {
                return value.getTimeUnit();
            }
        }
        return TimeUnit.SECONDS;
    }
}
