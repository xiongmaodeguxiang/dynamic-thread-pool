package com.zl.learn.threads.untils;

import com.alibaba.fastjson.JSON;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PropertiesUtil {

    public static  <T> List<T> getList(String prefix, Map<String,Object> properties, Class<T> type){
        if(StringUtils.isEmpty(prefix)){
            return new ArrayList<>();
        }
        String[] keyArr = prefix.split("\\.");
        Object value = null;
        for(int i = 0; i < keyArr.length; i++){
            String key = keyArr[i];
            if(StringUtils.isEmpty(key)){
                continue;
            }
            value = properties.get(key);
            if(Objects.isNull(value)){
                return new ArrayList<>();
            }
            if(i == keyArr.length - 1){
                break;
            }
            properties = (Map<String, Object>) properties.get(key);
        }
        if(Objects.isNull(value)){
            return new ArrayList<>();
        }
        return JSON.parseArray(JSON.toJSONString(value), type);
    }

    public static  String getString(String prefix, Map<String,Object> properties){
        if(StringUtils.isEmpty(prefix)){
            return null;
        }
        String[] keyArr = prefix.split("\\.");
        Object value = null;
        for(int i = 0; i < keyArr.length; i++){
            String key = keyArr[i];
            if(StringUtils.isEmpty(key)){
                continue;
            }
            value = properties.get(key);
            if(Objects.isNull(value)){
                return null;
            }
            if(i == keyArr.length - 1){
                break;
            }
            properties = (Map<String, Object>) properties.get(key);
        }
        if(Objects.isNull(value)){
            return null;
        }
        return String.valueOf(value);
    }
    public static  Integer getInt(String prefix, Map<String,Object> properties){
        if(StringUtils.isEmpty(prefix)){
            return null;
        }
        String[] keyArr = prefix.split("\\.");
        Object value = null;
        for(int i = 0; i < keyArr.length; i++){
            String key = keyArr[i];
            if(StringUtils.isEmpty(key)){
                continue;
            }
            value = properties.get(key);
            if(Objects.isNull(value)){
                return null;
            }
            if(i == keyArr.length - 1){
                break;
            }
            properties = (Map<String, Object>) properties.get(key);
        }
        if(Objects.isNull(value)){
            return null;
        }
        return Integer.valueOf((String) value);
    }
    public static  Long getLong(String prefix, Map<String,Object> properties){
        if(StringUtils.isEmpty(prefix)){
            return null;
        }
        String[] keyArr = prefix.split(".");
        Object value = null;
        for (String key : keyArr){
            if(StringUtils.isEmpty(key)){
                continue;
            }
            value = properties.get(key);
            if(Objects.isNull(value)){
                return null;
            }
        }
        if(Objects.isNull(value)){
            return null;
        }
        return Long.valueOf((String) value);
    }

}
