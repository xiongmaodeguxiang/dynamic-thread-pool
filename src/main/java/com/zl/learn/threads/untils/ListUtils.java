package com.zl.learn.threads.untils;

import com.zl.learn.threads.events.MetadataAddEvent;
import com.zl.learn.threads.events.MetadataChangeEvent;
import com.zl.learn.threads.events.MetadataDeleteEvent;
import com.zl.learn.threads.events.MetadataEvent;
import com.zl.learn.threads.executor.ExecutorMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtils {
    public static List<MetadataEvent> getMetadataChangeEvents(List<ExecutorMetadata> oldData, List<ExecutorMetadata> newData) {
        List<MetadataEvent> events = new ArrayList<MetadataEvent>();
        Map<String, ExecutorMetadata> oldExecutorMap = oldData.stream().collect(Collectors.toMap(ExecutorMetadata::getName, Function.identity(), (o1, o2) -> o2));
        Map<String, ExecutorMetadata> newExecutorMap = newData.stream().collect(Collectors.toMap(ExecutorMetadata::getName, Function.identity(), (o1, o2) -> o2));
        //查找新增
        for (ExecutorMetadata newDatum : newData) {
            if(!oldExecutorMap.containsKey(newDatum.getName())){//新增
                events.add(new MetadataAddEvent(newDatum));
            }else{//判断是否相等
                ExecutorMetadata oldExecutorMetadata = oldExecutorMap.get(newDatum.getName());
                if(!oldExecutorMetadata.equals(newDatum)){//发生了变更
                    events.add(new MetadataChangeEvent(oldExecutorMetadata, newDatum));
                }
            }
        }
        //寻找删除
        for (ExecutorMetadata oldDatum : oldData) {
            if(!newExecutorMap.containsKey(oldDatum.getName())){//删除
                events.add(new MetadataDeleteEvent(oldDatum));
            }
        }
        return events;
    }
}
