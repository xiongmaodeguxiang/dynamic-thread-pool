package com.zl.learn.threads.events;

import com.zl.learn.threads.executor.ExecutorMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MetadataChangeEvent extends MetadataEvent{
    private ExecutorMetadata oldMetadata;
    private ExecutorMetadata newMetadata;
}
