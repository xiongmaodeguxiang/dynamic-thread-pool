package com.zl.learn.threads.events;

import org.springframework.context.ApplicationEvent;

public class ConfigChangeEvent extends ApplicationEvent {
    private String content;
    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ConfigChangeEvent(String source) {
        super(source);
        this.content = source;
    }

    public String getContent() {
        return content;
    }
}
