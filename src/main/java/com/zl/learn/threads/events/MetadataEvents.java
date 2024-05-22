package com.zl.learn.threads.events;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class MetadataEvents extends ApplicationEvent {
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public MetadataEvents(List<MetadataEvent> source) {
        super(source);
    }
}
