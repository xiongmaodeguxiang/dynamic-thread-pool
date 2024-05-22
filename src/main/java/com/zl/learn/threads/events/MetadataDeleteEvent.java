package com.zl.learn.threads.events;

import com.zl.learn.threads.executor.ExecutorMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MetadataDeleteEvent extends MetadataEvent{
    private ExecutorMetadata metadata;
}
