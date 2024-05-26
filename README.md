## 基于Nacos配置的动态线程池
1.该项目包含了对核心线程，最大线程数，阻塞队列，拒绝策略等参数的修改
2.项目继承了prometheus实现了线程池监控，可以配置线程池中活跃线程与最大线程比例达到阈值时进行报警以及自动恢复通知
3.可以基于prometheus的数据搭建grafana来线程池进行监控
## 实现原理
1.通过对nacos配置文件的监听来实现线程池参数的修改，自定义阻塞队列，支持阻塞队列的扩容与类型切换
2.通过prometheus上报监控指标，监控指标为：
 - core_size_monitor{executor_name="aa",} 2.0 核心线程数监控，并且executor_name标签为线程池名字
 - max_size_monitor{executor_name="aa",} 10.0 最大线程数指标
 - active_size_monitor{executor_name="aa",} 10.0 正在执行任务的线程数指标
 - pool_size_monitor{executor_name="aa",} 10.0 线程池中存在线程数的指标
 - queue_size_monitor{executor_name="aa",} 91.0 线程池队列中元素数量的指标监控
 同时使用prometheus以及alertmanager配置报警规则：对active_size_monitor/max_size_monitor指标进行监控从而来实现线程池的报警与自动恢复
 3.通过grafana拉去prometheus的数据可以生成线程池中各个指标的折线图
 ## 接入流程
 项目中引入依赖：
 ```xml
    <dependency>
      <groupId>com.zl.learn</groupId>
      <artifactId>dynamic-thread-pool</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
```
由于prometheus是基于consul来监控服务的因此，我们使用consul作为注册中心，可以参考我示例的配置文件
```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.3.5.RELEASE</version>
    <relativePath/>
  </parent>
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.boot.learn</groupId>
  <artifactId>springboot-project</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <spring-cloud-alibaba.version>2.2.6.RELEASE</spring-cloud-alibaba.version>
    <spring-cloud.version>Hoxton.SR10</spring-cloud.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>1.2.76</version>
    </dependency>
    <!-- lombok 插件-->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.12</version>
    </dependency>
    <dependency>
      <groupId>com.zl.learn</groupId>
      <artifactId>dynamic-thread-pool</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>
  </dependencies>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>${spring-cloud-alibaba.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>

```
同时bootstrap.yaml中配置consul的信息
```yaml
spring:
  cloud:
    consul:
      host: 127.0.0.1
      port: 8500
      discovery:
        enabled: true
        register: true
        service-name: ${spring.application.name}
        instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}
        prefer-ip-address: true
        health-check-path: /actuator/health
        health-check-interval: 10s
        query-passing: true
```
application.yml文件中配置服务信息
```yaml
server:
  port: 9007
nacos: #nacos的配置信息
  server-addr: localhost:8848
  namespace: public
management:
  endpoint:
    prometheus:
      enabled: true
  endpoints:
    web:
      exposure:
        include:
          - prometheus #打开prometheus指标路径
          - health
spring:
  application:
    name: thread-demo
```
服务启动类添加注解
```java
@SpringBootApplication
@EnableDynamicExecutor
@EnableDiscoveryClient
public class Application
{
    public static void main( String[] args ){
        SpringApplication.run(Application.class,args);
    }
}
```
需要在nacos中配置监听文件，文件的dataId为:${spring.application.name}-executor.yaml，groupId为EXECUTOR
比如上边配置的spring.application.name为thread-demo，则dataId为thread-demo-executor.yaml
文件示例：
```yaml
dynamic:
  executors:
    - name: aa ##线程池的名字
      core: 2
      max: 10
      queueType: 4
      queueSize: 100
      keepAliveTime: 100
      handlerType: 2
    - name: bb
      core: 2
      max: 10
      queueType: 4
      queueSize: 100
      keepAliveTime: 100
      handlerType: 2
```
测试代码如下：
```java
    package com.boot.learn.controller;
    
    import com.zl.learn.threads.executor.ExecutorInstances;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;
    
    import java.util.concurrent.ThreadPoolExecutor;
    import java.util.concurrent.TimeUnit;
    
    /**
     * @author: zhanglin574@xdf.cn
     * @date: 2024/1/23
     * @description:
     */
    @RestController
    public class ThreadTestController {
        @Autowired
        ExecutorInstances executorInstances;
    
        @GetMapping("/test")
        public String test() throws InterruptedException {
            ThreadPoolExecutor executor = executorInstances.getExecutor("aa");
            for (int i = 0; i < 50000; i++){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            System.out.println(Thread.currentThread().getName());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return "我是test";
        }
        @GetMapping("/test2")
        public String test2() throws InterruptedException {
            ThreadPoolExecutor executor = executorInstances.getExecutor("bb");
            for (int i = 0; i < 50000; i++){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            System.out.println(Thread.currentThread().getName());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            return "我是test";
        }
    }
```
至此微服务的配置已完成
## prometheus监控配置
由于我们使用prometheus收集了指标，因此我们现在需要使用prometheus来实现报警，首先需要搭建prometheus服务，我采用的是docker搭建的
```shell script
docker run \
  --name prometheus \
  -p 9090:9090 \
  -v /Users/ao_qian/Desktop/tools/promethus:/etc/prometheus \
  -v /Users/ao_qian/Desktop/tools/promethus/data:/prometheus \
  prom/prometheus
```
然后在promethues目录下创建prometheus.yml文件以及alert.rules文件
prometheus.yml文件
```yaml
global:
  scrape_interval:     15s # By default, scrape targets every 15 seconds.

  # Attach these labels to any time series or alerts when communicating with
  # external systems (federation, remote storage, Alertmanager).
  external_labels:
    monitor: 'codelab-monitor'
alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - '192.168.11.102:9093'
          
# A scrape configuration containing exactly one endpoint to scrape:
# Here it's Prometheus itself.
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: 'prometheus'

    # Override the global default and scrape targets from this job every 5 seconds.
    scrape_interval: 5s

    static_configs:
      - targets: ['localhost:9090']
  - job_name: 'consul-service-monitoring'
    # Override the global default and scrape targets from this job every 5 seconds.
    scrape_interval: 5s
    metrics_path: '/actuator/prometheus'
    consul_sd_configs:
      - server: '192.168.11.102:8500'
        services: []  # 替换为你的微服务名称
    relabel_configs:
      - source_labels: ['__meta_consul_service']
        target_label:  'application'
      - source_labels: ['__address__']
        target_label:  'instance'
      - source_labels: ['__meta_consul_node']
        target_label:  'node'
      - source_labels: ['__meta_consul_tags']
        target_label:  'tags'
      - source_labels: ['__meta_consul_service_metadata']
        target_label: 'metadata'
    # metric_relabel_configs:
    #   - source_labels: ['metadata']
    #     regex: '.*'
    #     action: keep
    honor_labels: true
    params:
      metric[]:
        - '{__name__=~".+"}'
rule_files:
  - "alert.rules"
```
alert.rules文件的内容为：
```yaml
groups:
- name: threadpool_alerts
  rules:
  # 计算每个executor_name的core_size_monitor与max_size_monitor的比值
  - record: threadpool_usage_ratio:ratio
    expr: active_size_monitor / max_size_monitor
  # 报警规则，当比值为80%时触发
  - alert: ThreadPoolUsageHigh
    expr: threadpool_usage_ratio:ratio > 0.8
    for: 20s
    labels:
      severity: critical
    annotations:
      summary: "High thread pool usage"
      description: "The thread pool usage ratio for application {{ $labels.application }}, instance {{$labels.instance }}, and executor_name {{ $labels.executor_name }} is above 80%."
```
至此promethues搭建完成，然后还需要搭建malertmanager来进行报警，使用docker搭建容器
```shell script
docker run -d \
  --name alertmanager \
  -p 9093:9093 \
  -v /Users/ao_qian/Desktop/tools/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml \
  prom/alertmanager
```
alertmanager.yml内容
```yaml
route:
  receiver: 'default-receiver'
  group_by: ['severity']
  group_wait: 5s
  group_interval: 2s
  repeat_interval: 10s

receivers:
- name: 'default-receiver'
  email_configs:
  - to: '1107229936@qq.com'
    from: '1107229936@qq.com'
    smarthost: 'smtp.qq.com:465'
    auth_username: '1107229936@qq.com'
    auth_password: '**********' #自己邮箱的授权密令，不是qq密码
    send_resolved: true
    require_tls: false
```
至此alertmanager服务器也搭建完成

## grafana搭建
使用docker搭建grafana容器
```shell script
docker pull grafana/grafana

 mkdir -p /Users/ao_qian/Desktop/tools/grafana/{data,config,plugins}

chmod -R 777 /Users/ao_qian/Desktop/tools/grafana/data
chmod -R 777 /Users/ao_qian/Desktop/tools/grafana/config
chmod -R 777 /Users/ao_qian/Desktop/tools/grafana/plugins

# 先临时启动一个容器
docker run --name grafana -d -p 3000:3000 grafana/grafana
# 将容器中默认的配置文件拷贝到宿主机上
docker cp grafana:/etc/grafana/grafana.ini /Users/ao_qian/Desktop/tools/grafana/config/grafana.ini
# 移除临时容器
docker stop grafana

docker rm grafana

# 启动grafana
# 环境变量GF_SECURITY_ADMIN_PASSWORD：指定admin的密码
# 环境变量GF_INSTALL_PLUGINS：指定启动时需要安装得插件
#         grafana-clock-panel代表时间插件
#         grafana-simple-json-datasource代表json数据源插件
#         grafana-piechart-panel代表饼图插件

docker run -d \
    -p 3000:3000 \
    --name=grafana \
    -v /etc/localtime:/etc/localtime:ro \
    -v /Users/ao_qian/Desktop/tools/grafana/data:/var/lib/grafana \
    -v /Users/ao_qian/Desktop/tools/grafana/plugins/:/var/lib/grafana/plugins \
    -v /Users/ao_qian/Desktop/tools/grafana/config/grafana.ini:/etc/grafana/grafana.ini \
    -e "GF_SECURITY_ADMIN_PASSWORD=admin" \
    -e "GF_INSTALL_PLUGINS=grafana-clock-panel,grafana-simple-json-datasource,grafana-piechart-panel" \
    grafana/grafana
```
容器搭建完成后可以创建dashboard，dashboard的jsonmode如下：
```json
{
  "annotations": {
    "list": [
      {
        "builtIn": 1,
        "datasource": {
          "type": "grafana",
          "uid": "-- Grafana --"
        },
        "enable": true,
        "hide": true,
        "iconColor": "rgba(0, 211, 255, 1)",
        "name": "Annotations & Alerts",
        "type": "dashboard"
      }
    ]
  },
  "editable": true,
  "fiscalYearStartMonth": 0,
  "graphTooltip": 0,
  "id": 7,
  "links": [],
  "panels": [
    {
      "datasource": {
        "type": "prometheus",
        "uid": "fdmjxb3gexgjka"
      },
      "fieldConfig": {
        "defaults": {
          "color": {
            "mode": "palette-classic"
          },
          "custom": {
            "axisBorderShow": false,
            "axisCenteredZero": false,
            "axisColorMode": "text",
            "axisLabel": "",
            "axisPlacement": "auto",
            "barAlignment": 0,
            "drawStyle": "line",
            "fillOpacity": 0,
            "gradientMode": "none",
            "hideFrom": {
              "legend": false,
              "tooltip": false,
              "viz": false
            },
            "insertNulls": false,
            "lineInterpolation": "linear",
            "lineWidth": 1,
            "pointSize": 5,
            "scaleDistribution": {
              "type": "linear"
            },
            "showPoints": "auto",
            "spanNulls": false,
            "stacking": {
              "group": "A",
              "mode": "none"
            },
            "thresholdsStyle": {
              "mode": "off"
            }
          },
          "mappings": [],
          "thresholds": {
            "mode": "absolute",
            "steps": [
              {
                "color": "green",
                "value": null
              },
              {
                "color": "red",
                "value": 80
              }
            ]
          }
        },
        "overrides": []
      },
      "gridPos": {
        "h": 8,
        "w": 12,
        "x": 0,
        "y": 0
      },
      "id": 1,
      "options": {
        "legend": {
          "calcs": [],
          "displayMode": "list",
          "placement": "bottom",
          "showLegend": true
        },
        "tooltip": {
          "mode": "single",
          "sort": "none"
        }
      },
      "targets": [
        {
          "datasource": {
            "type": "prometheus",
            "uid": "fdmjxb3gexgjka"
          },
          "disableTextWrap": false,
          "editorMode": "code",
          "expr": "core_size_monitor{application=\"$application\", instance=\"$instance\", executor_name=\"$executor_name\"}",
          "fullMetaSearch": false,
          "includeNullMetadata": true,
          "instant": false,
          "legendFormat": "__auto",
          "range": true,
          "refId": "A",
          "useBackend": false
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "fdmjxb3gexgjka"
          },
          "disableTextWrap": false,
          "editorMode": "code",
          "expr": "label_replace(active_size_monitor{application=\"$application\", instance=\"$instance\", executor_name=\"$executor_name\"},\"core_label\",\"$1\",\"__name__\",\"(.*)\")",
          "fullMetaSearch": false,
          "hide": false,
          "includeNullMetadata": true,
          "instant": false,
          "legendFormat": "__auto",
          "range": true,
          "refId": "B",
          "useBackend": false
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "fdmjxb3gexgjka"
          },
          "editorMode": "code",
          "expr": "max_size_monitor{application=\"$application\", instance=\"$instance\", executor_name=\"$executor_name\"}",
          "hide": false,
          "instant": false,
          "legendFormat": "__auto",
          "range": true,
          "refId": "C"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "fdmjxb3gexgjka"
          },
          "editorMode": "code",
          "expr": "pool_size_monitor{application=\"$application\", instance=\"$instance\", executor_name=\"$executor_name\"}",
          "hide": false,
          "instant": false,
          "legendFormat": "__auto",
          "range": true,
          "refId": "D"
        },
        {
          "datasource": {
            "type": "prometheus",
            "uid": "fdmjxb3gexgjka"
          },
          "editorMode": "code",
          "expr": "queue_size_monitor{application=\"$application\", instance=\"$instance\", executor_name=\"$executor_name\"}",
          "hide": false,
          "instant": false,
          "legendFormat": "__auto",
          "range": true,
          "refId": "E"
        }
      ],
      "title": "Panel Title",
      "type": "timeseries"
    }
  ],
  "refresh": "5s",
  "schemaVersion": 39,
  "tags": [],
  "templating": {
    "list": [
      {
        "current": {
          "selected": false,
          "text": "spring-boot",
          "value": "spring-boot"
        },
        "datasource": {
          "type": "prometheus",
          "uid": "fdmjxb3gexgjka"
        },
        "definition": "label_values(application)",
        "hide": 0,
        "includeAll": false,
        "label": "application",
        "multi": false,
        "name": "application",
        "options": [],
        "query": {
          "qryType": 1,
          "query": "label_values(application)",
          "refId": "PrometheusVariableQueryEditor-VariableQuery"
        },
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 0,
        "type": "query"
      },
      {
        "current": {
          "selected": false,
          "text": "192.168.11.102:9005",
          "value": "192.168.11.102:9005"
        },
        "datasource": {
          "type": "prometheus",
          "uid": "fdmjxb3gexgjka"
        },
        "definition": "label_values(jvm_buffer_memory_used_bytes{application=\"$application\"},instance)",
        "hide": 0,
        "includeAll": false,
        "label": "instance",
        "multi": false,
        "name": "instance",
        "options": [],
        "query": {
          "qryType": 1,
          "query": "label_values(jvm_buffer_memory_used_bytes{application=\"$application\"},instance)",
          "refId": "PrometheusVariableQueryEditor-VariableQuery"
        },
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 0,
        "type": "query"
      },
      {
        "current": {
          "selected": false,
          "text": "aa",
          "value": "aa"
        },
        "datasource": {
          "type": "prometheus",
          "uid": "fdmjxb3gexgjka"
        },
        "definition": "label_values(core_size_monitor{application=\"$application\", instance=\"$instance\"},executor_name)",
        "hide": 0,
        "includeAll": false,
        "label": "executor_name",
        "multi": false,
        "name": "executor_name",
        "options": [],
        "query": {
          "qryType": 1,
          "query": "label_values(core_size_monitor{application=\"$application\", instance=\"$instance\"},executor_name)",
          "refId": "PrometheusVariableQueryEditor-VariableQuery"
        },
        "refresh": 1,
        "regex": "",
        "skipUrlSync": false,
        "sort": 0,
        "type": "query"
      }
    ]
  },
  "time": {
    "from": "now-3h",
    "to": "now"
  },
  "timepicker": {},
  "timezone": "browser",
  "title": "test_dashboard",
  "uid": "bdmq73hcrpr0ga",
  "version": 8,
  "weekStart": ""
}
```
当然直接拷贝不行，需要自己先创建一个空的dashboard然后需要将json中panels与templating中的内容替换到空的dashboard中
至此线程池的grafana监控也搭建完毕

