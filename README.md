# 服务注册中心

## Eureka（停更）

Eureka是Netflix开发的一个服务发现框架，SpringCloud将它集成在其子项目`spring-cloud-netflix`中，主要的功能是`服务注册`和`服务发现`。

Eureka有两个组件，对应了两个角色。服务中心和服务的客户端。

### Eureka Server

引入依赖。

```xml
	<dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
```

编写配置文件。

```yml
eureka:
  instance:
    # eureka服务端的实例名称
    hostname: localhost:7001
  client:
    # false表示不向注册中心注册自己这个微服务
    register-with-eureka: false
    # false表示当前服务是注册中心，并不需要去检索服务
    fetch-registry: false
    service-url:
      # 设置与Eureka Server交互的地址查询服务和注册服务依赖的地址
      defaultZone: http://localhost:7001/eureka
```

开启Eureka Server服务。

```Java
@EnableEurekaServer
@SpringBootApplication
public class EurekaMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaMain.class, args);
    }
}
```

### Eureka Client

引入依赖。

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
```

编写配置文件。

```yml
eureka:
  client:
    # 表示是否将当前服务注册进注册中心，默认为true
    register-with-eureka: true
    # 是否从注册中心抓取已有的注册信息，默认为true。单节点无所谓，集群一定要为true才能配合ribbon使用负载均衡
    fetch-registry: true
    service-url:
      # 服务中心的地址
      defaultZone: http://localhost:7001/eureka
  instance:
    # 实例名称
    instance-id: payment8001
    # 访问路径显示IP地址
    prefer-ip-address: true
```

开启服务。

```Java
@EnableEurekaClient
@SpringBootApplication
public class PaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class, args);
    }
}
```

### Eureka的集群

集群的配置主要就是要注意，两两注册。

两个Server端。

```yml
eureka:
  instance:
    hostname: eureka7001.com
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      # 集群就指向其他服务中心的地址，单机版就指向自己
      defaultZone: http://eureka7002.com:7002/eureka
```

```yml
eureka:
  instance:
    hostname: eureka7002.com
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      # 集群就指向其他服务中心的地址，单机版就指向自己
      defaultZone: http://eureka7001.com:7001/eureka
```

Client端的配置。

```yml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      # 将全部的注册中心的地址写入
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
  instance:
    instance-id: payment8001
    prefer-ip-address: true
```



### 服务发现

通过发现Client的服务发现，来获取Client端的服务信息。

主启动类中开启服务发现。

```Java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class PaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class, args);
    }
}
```

服务发现的简单使用。

```Java
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    /**
     * 引入服务发现
     */
    @Resource
    private DiscoveryClient discoveryClient;

    /**
     * 简单地获取服务和实例信息
     */
    @GetMapping("/discovery")
    public Result<DiscoveryClient> discovery() {
        // 获取注册的微服务列表信息
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            log.info("服务------" + service);
        }
        // 根据具体的微服务ID获取其中所有的实例
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-PROVIDER");
        for (ServiceInstance instance : instances) {
            log.info("服务ID-----" + instance.getServiceId());
            log.info("主机名称-----" + instance.getHost());
            log.info("实例端口号-----" + instance.getPort());
            log.info("实例地址" + instance.getUri());
        }
        return new Result<>(200,"获取信息成功",this.discoveryClient);
    }
    
}
```

### Eureka的自我保护

#### 自我保护机制

在Eureka的服务注册中心会看到一行红字，看到了这段红字提示，就代表Eureka进入了自我保护机制。

> EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.

简单的机器翻译为。

> 紧急情况！ EUREKA 可能不正确地声称实例已启动，但实际上并非如此。续订少于阈值，因此实例不会只是为了安全而过期。

保护模式主要用于一组客户端和服务端之间存在网络分区场景下的保护，一旦进入保护模式，服务端将会尝试保护器服务注册表中的信息，不删除服务注册中的数据，也就是不会注销任何微服务。简单来说就是服务注册中心中的某一个Client微服务不可用了，Eureka并不会立刻清理该微服务信息，依旧会保存在服务注册中心。



> 默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，Eureka会注销该实例（默认90秒）。
>
> 如果出现了网络分区的情况，Client与Server之间无法正常通信，这样的注销行为就会很不安全，因为有可能是因为网络问题导致无法接收到心跳，而不是该Client状态不健康，就不应该立刻注销该微服务。Eureka通过自我保护模式来解决了这个问题。
>
> 当EurekaServer节点短时间内丢失过多Client时，该节点就会进入自我保护模式。自我保护模式中，Server不会注销任何服务实例，即使是不健康的实例，也会保留。

所以Eureka属于CAP分支中的`AP`分支。

#### 如何关闭自我保护

修改配置就可以关闭自我保护。

```yml
eureka:
  instance:
    hostname: eureka7001.com
  server:
    # 关闭自我保护机制,默认是开启的
    enable-self-preservation: false
    # 间隔时间
    eviction-interval-timer-in-ms: 2000
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://eureka7002.com:7002/eureka
```

设置客户端向服务端发送心跳配置。

```yml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
  instance:
    instance-id: payment8001
    prefer-ip-address: true
    # EurekaClient向服务端发送心跳的时间间隔，默认为30秒，单位为秒
    lease-renewal-interval-in-seconds: 30
    # Eureka服务端在收到最后一次心跳后的等待时间上限，超时就剔除，默认为90秒，单位为秒
    lease-expiration-duration-in-seconds: 90
```

## Consul

### 简介

官网地址<https://www.consul.io/>

官网的介绍。

> Consul 是一种服务网格解决方案，提供具有服务发现、配置和分段功能的全功能控制平面。这些功能中的每一个都可以根据需要单独使用，也可以一起使用以构建完整的服务网格。
>
> Consul 需要一个数据平面并支持代理和本地集成模型。
>
> Consul 附带一个简单的内置代理，因此一切都可以开箱即用，而且还支持 3rd 方代理集成，例如 Envoy。

主要特点。

> 1. 服务发现：Consul 的客户端可以注册一个服务，例如 api或mysql，其他客户端可以使用 Consul 来发现给定服务的提供者。使用 DNS 或 HTTP，应用程序可以轻松找到它们所依赖的服务。
>
> 2. 健康检查：Consul 客户端可以提供任意数量的健康检查，要么与给定的服务相关联（“网络服务器是否返回 200 OK”），要么与本地节点（“内存利用率低于 90%”）相关联。操作员可以使用此信息来监视集群健康状况，并且服务发现组件可以使用它来将流量从不健康的主机路由出去。
>
> 3. KV 存储：应用程序可以将 Consul 的分层键/值存储用于多种目的，包括动态配置、功能标记、协调、领导选举等。简单的 HTTP API 使其易于使用。
>
> 4. 安全服务通信：Consul 可以为服务生成和分发 TLS 证书，以建立相互的 TLS 连接。 意图 可用于定义允许哪些服务进行通信。可以通过实时更改意图轻松管理服务分段，而不是使用复杂的网络拓扑和静态防火墙规则。
>
> 5. 多数据中心：Consul 支持开箱即用的多个数据中心。这意味着 Consul 的用户不必担心构建额外的抽象层以扩展到多个区域。



### 下载使用

win10下载之后就只有一个`consul.exe`文件，直接在文件夹中使用CMD即可。

```cmd
	# 查看版本信息
	consul -version
	# 使用开发模式启动consul
	consul agent -dev
```

然后访问consul的首页。<http://localhost:8500>

### 注册服务

引入依赖。

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-consul-discovery</artifactId>
        </dependency>
```

修改配置文件。

```yml
server:
  port: 8006

spring:
  application:
    name: consul-provider-payment
  cloud:
    # consul注册中心地址
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
```

开启服务发现。

```Java
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentMain {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMain.class, args);
    }

}
```

在Consul的控制台就能查看到该服务的该实例。



## 服务注册中心异同点

|  组件名   | 实现语言 | CAP  | 服务健康检查 | 对外暴露接口 | SpringCloud集合 |
| :-------: | :------: | :--: | :----------: | :----------: | :-------------: |
|  Eureka   |   Java   |  AP  |   可配支持   |     HTTP     |      集成       |
|  Consul   |    GO    |  CP  |     支持     |   HTTP/DNS   |      集成       |
| Zookeeper |   Java   |  CP  |     支持     |    客户端    |      集成       |



# 服务调用

## Ribbon（维护模式）

Ribbon是基于Neftlix Ribbon实现的一套客户端负载均衡工具，主要功能是提供客户端的软件负载均衡算法和服务调用。

工作流程。

1. 先选择EurekaServer，优先选择在同一个区域内负载较少的server
2. 在根据用户指定的策略，再从server获取的服务注册列表选择一个地址

Ribbon就是负载均衡加上RestTemplate调用。

### 使用Ribbon

引入依赖。

```xml
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>

	<!--  只要引入了这个client依赖，就不用引入上面的ribbon依赖了
		lient是包含了ribbon依赖的，不想用eureka就直接引入上面的ribbon依赖  -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
```

Ribbon是基于RestTemplate调用实现的，所以要使用Ribbon还需要去RestTemplate配置类上开启。

```Java
@Configuration
public class RestConfig {

    /**
     *  LoadBalanced开启负载均衡
     */
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
```

### Ribbon常见的负载均衡实现

Ribbon的核心接口IROLE，用于制定一个负载均衡规则的实现。

```Java
public interface IRule{
    /*
     * choose one alive server from lb.allServers or
     * lb.upServers according to key
     * 
     * @return choosen Server object. NULL is returned if none
     *  server is available 
     */

    public Server choose(Object key);
    
    public void setLoadBalancer(ILoadBalancer lb);
    
    public ILoadBalancer getLoadBalancer();    
}
```

它的一些实现类就是一些常用的负载均衡规则。

1. `com.netflix.loadbalancer.RoundRobinRule`

   轮询

2. `com.netflix.loadbalancer.RandomRule`

   随机

3. `com.netflix.loadbalancer.RetryRule`

   先按照轮询的策略获取服务，如果获取服务失败则在指定时间内进行重试，获取可用的服务

4. `WeightedResponseTimeRule`

   对轮询的扩展，响应速度越快的实例选择权重越大，越容易被选择

5. `BestAvailableRule`

   会过滤调由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务

6. `AvailabilityFilteringRule`

   先过滤故障实例，再选择并发较小的实例

7. `ZoneAvoidanceRule`

   默认规则，复合判断server所在区域的性能和server的可用性选择服务器

### 修改负载均衡规则

官方说明不能被`@ComponentScan`注解给扫描到，而`@SpringBootApplication`启动类注解中是包含了`@ComponentScan`的，所以不能放在主启动类的包下。

创建配置类。

```Java
/**
 * <p>
 *     自定义负载均衡规则类
 * </p>
 *
 * @author yww
 * @version 1.0
 **/
@Configuration
// 使用MyRule类名可能会出现问题
public class MySelfRule {
    @Bean
    public IRule myRule() {
        return new RandomRule();
    }
}
```

然后在主启动类下开启自定义规则。

```Java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(name = "CLOUD-PAYMENT-PROVIDER", configuration = MySelfRule.class)
public class OrderMain {
    public static void main(String[] args) {
        SpringApplication.run(OrderMain.class,args);
    }
}
```

### 轮询算法的实现

轮询的原理就是通过取模，确定请求的位置，类似于hash的实现。

实现的两个方法。（com.netflix.loadbalancer.RoundRobinRule）

```Java
    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (server == null && count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }
```

## Feign

这个暂时不考虑。

## OpenFeign

Feign是一个声明式WebService客户端，使用在服务消费端。

前面使用的是Ribbon+RestTemplate的服务调用方法，利用RestTemplate对http请求进行封装处理，调用其他服务的接口。Feign在此的基础上进一步封装，只需要添加一个注解，即可实现服务调用。

Feign是SpringCloud组件中一个轻量级的RESTful的HTTP服务客户端，Feign内置了Ribbon，用来做客户端负载均衡。

OpenFeign是在Feign的基础上支持了SpringMVC的注解，实现类中做负载均衡并调用其他服务。

### 简单使用

引入依赖。

```
	<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```

这个依赖是包含了Ribbon依赖的，所以使用其中的负载均衡功能。

编写配置文件。

```yml
server:
  port: 80
spring:
  application:
    name: feign-payment-consumer

eureka:
  client:
    register-with-eureka: false
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka
```

开启使用。

```Java
@SpringBootApplication
@EnableFeignClients
public class FeignOrderMain {
    public static void main(String[] args) {
        SpringApplication.run(FeignOrderMain.class, args);
    }
}
```

创建业务服务接口。即通过这个接口，Feign就会调用指定服务的接口。

```Java
/**
 * <p>
 *     业务服务接口
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 18:55
 **/
// 指定调用的服务（服务提供者）
@FeignClient("CLOUD-PAYMENT-PROVIDER")
public interface PaymentFeignService {
    
    /**
     * 通过ID获取订单（调用指定服务的指定接口）
     * @param id    数据ID
     * @return      订单信息
     */
    @GetMapping("/payment/getById/{id}")
    Result<Payment> getById(@PathVariable("id") Long id);

}
```

前端控制器调用业务接口。

```Java
@RestController
@Slf4j
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private PaymentFeignService service;
    @GetMapping("getById/{id}")
    public Result<Payment> getById(@PathVariable("id")Long id) {
        return service.getById(id);
    }

}
```

总结。

1. 主启动类上开启使用Feign。(@EnableFeignClients)
2. 编写service业务接口类，指定调用的服务提供者。(@FeignClient("CLOUD-PAYMENT-PROVIDER"))
3. 编写的抽象接口就使用服务提供者的接口。

### 超时控制

因为是调用服务提供者的接口，所以就可能会出现超时的情况，可用通过配置进行超时控制。

就比如Feign默认的配置超时时间是1秒，即调用的接口1秒之内没有结果回应，Feign客户端就不会继续等待了，直接返回报错。

编写配置类

```Java
feign:
  client:
    config:
	  # 单位ms 
      default:
        # 建立连接后从服务器读取到可用资源所用的时间
        connectTimeout: 5000
        # 建立连接建立所用时间
        readTimeout: 5000
```

### 日志打印功能

OpenFeign日志级别。

1. NONE

   默认的，不显示任何日志

2. BASIC

   仅记录请求方法，URL，响应状态码及执行时间

3. HEADERS

   除了BASIC中定义的信息之外，还有请求和响应的头信息

4. FULL

   除了HEADERS中定义的信息之外，还有请求和响应的正文及元数据

编写配置类。

```Java
/**
 * <p>
 *     Feign的配置类
 * </p>
 *
 * @author yww
 **/
@Configuration
public class FeignConfig {
    
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
}
```

设置需要打印日志的接口。

```yml
logging:
  level:
    # 业务接口类，debug是等级 
    com.yww.springcloud.service.PaymentFeignService: debug
```



# 服务降级

## Hystrix（维护模式）

> 服务雪崩
>
> 多个微服务之间的调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其他的微服务，这就是所谓的“扇出”。
>
> 如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃，这就是所谓的雪崩效应。

Hystrix是一个用于处理分布式系统的延迟和容错的开源库，比如在分布式系统里，许多依赖不可避免地会调用失败（超时，异常等），Hystrix就是用于保证不会因为一个依赖出现问题而导致整个服务失败地情况，避免级联故障。

当一个服务发生了故障，无法返回结果，就会通过断路器地故障监控，向调用方返回一个符合预期地备选响应，而不是直接导致整个服务调用的失败。

Hystrix的主要功能。

1. 服务降级
2. 服务熔断
3. 接近实时的监控

### 一些重要的概念

- 服务降级

  服务的负载过大，但是资源是有限的，根据降级的策略牺牲一些服务或者是功能，保证核心服务的正常执行。

- 服务熔断

  当上游服务因为某种原因而调用失败，下游服务为了保证整体服务的可用性，就会不再继续调用失败的上游服务，返回一个设定的结果，快速释放资源。

- 服务限流

  服务限制请求的速率，比如服务设定了一个访问阈值，在一段时间内的访问次数超过了阈值，超过的部分就会直接拒绝访问。

> 降级和熔断
>
> 降级和熔断的概念很相似，但其实是不一样的。
>
> 降级强调的是整体架构的可用性，熔断强调的是当前服务的可用性。
>
> 降级的目的在于应对系统自身的故障，而熔断的目的在于应对当前系统依赖的外部系统或者第三方系统的故障。

### 前提业务

引入依赖。

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

编写业务实现类。

```Java
@Service
public class PaymentService {

    /**
     * 模拟正常访问的情况
     * @param id ID
     * @return  返回OK
     */
    public String paymentInfoOk(Integer id) {
        return "线程池" + Thread.currentThread().getName() + "访问成功, id为: " + id;
    }

    /**
     * 模拟超时访问的情况
     * @param id ID
     * @return  返回Fault
     */
    public String paymentInfoTimeOut(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池" + Thread.currentThread().getName() + "超时访问, id为: " + id;
    }

}
```

编写前端控制器。

```Java
@RestController
@Slf4j
@RequestMapping("payment")
public class PaymentController {

    @Resource
    private PaymentService service;

    @Value("${server.port}")
    private String port;


    @GetMapping("/ok/{id}")
    public String infoOk(@PathVariable("id")Integer id) {
        return service.paymentInfoOk(id);
    }

    @GetMapping("/timeout/{id}")
    public String infoTimeOut(@PathVariable("id")Integer id) {
        return service.paymentInfoTimeOut(id);
    }

}
```

### 高压力测试

> 前提
>
> 访问正常请求接口，基本都是秒回信息。
>
> 访问超时请求接口，则会等待三秒钟才返回信息。

使用JMeter对超时请求接口进行压力测试。

模拟多线程多个请求访问超时请求接口，自己访问正常请求接口，会发现已经不是秒回信息了，可能会出现超时，停顿的情况。

因为tomcat的默认的工作线程数被打满了，没有多余的线程来分解压力和处理。

这还只是服务提供者的自测，若是还有服务消费者，会极大可能出现请求超时的情况，导致服务消费者的请求不能及时完成，拖死客户端。

正是因为这种情况的出现，所以需要我们进行降级，容错，限流等。

> 解决的问题
>
> 1. 服务端出现超时或者宕机了，调用者不能一直卡死等待，需要服务降级
>
> 2. 服务端成功返回，但是调用者自己出现故障或又自我要求，自己处理降级

### 服务提供者的服务降级

设置自身调用超时时间，不超过超时时间的就可以正常运行，超过了的需要用指定方法进行处理，作服务降级。

主启动类开启服务降级。

```Java
@SpringBootApplication
@EnableEurekaClient
// @EnableCircuitBreaker
@EnableCircuitBreaker
public class HystrixPaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixPaymentMain.class, args);
    }
}
```

配置业务接口的超时设置和超时的处理方式。

```Java
@Service
public class PaymentService {

    /**
     * 正常访问的情况
     * @param id ID
     * @return  返回OK
     */
    public String paymentInfoOk(Integer id) {
        return "线程池" + Thread.currentThread().getName() + "访问成功, id为: " + id;
    }

    /**
     * 超时访问的情况
     * 设置2秒钟之内可以正常执行，逻辑里停了3秒，模拟降级情况
     * 如果逻辑出现异常或者错误，不是超时的问题，也会进行降级
     * @param id ID
     * @return  返回Fault
     */
    @HystrixCommand(fallbackMethod = "timeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "2000")
    })
    public String paymentInfoTimeOut(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池" + Thread.currentThread().getName() + "超时访问, id为: " + id;
    }

    public String timeoutHandler(Integer id) {
        return "超时请求，请稍后再试！";
    }

}
```

### 服务消费者的服务降级

配置文件开启支持服务降级。

```yml
feign:
  hystrix:
    enabled: true
```

主启动类启用。

```Java
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class FeignHystrixMain {
    public static void main(String[] args) {
        SpringApplication.run(FeignHystrixMain.class, args);
    }
}
```

业务类接口处理。

```Java
@RestController
@Slf4j
@RequestMapping("consumer")
public class HystrixController {

    @Resource
    private FeignConsumer server;

    @GetMapping("/ok/{id}")
    public String ok(@PathVariable("id")Integer id) {
        return server.infoOk(id);
    }

    @GetMapping("/timeout/{id}")
    @HystrixCommand(fallbackMethod = "timeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1000")
    })
    public String timeout(@PathVariable("id")Integer id) {
        return server.infoTimeOut(id);
    }
    
    public String timeoutHandler(@PathVariable("id")Integer id) {
        return "消费端调用超时，请稍后再试！" + id;
    }

}
```

> 一般是调用端配置超时设置，在哪里配置或者两端都配置还要根据具体业务需求。

### 全局服务降级

刚刚演示的是某一个方法的服务降级，若是方法过多，就会导致降级处理的方法也会变多，所以大多数都是会用同一个降级处理方法。

1. 在controller的地方添加降级配置`@DefaultProperties`
2. 编写全局降级处理方法
3. 给需要降级的接口添加`@HystrixCommand`注解，不指定降级处理方法就使用控制器全局的配置降级方法。

```Java
@RestController
@Slf4j
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "globalTimeout", commandProperties = {
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1000")
})
public class HystrixController {

    @Resource
    private FeignConsumer server;

    @GetMapping("/ok/{id}")
    public String ok(@PathVariable("id")Integer id) {
        return server.infoOk(id);
    }

    @GetMapping("/timeout/{id}")
    @HystrixCommand(fallbackMethod = "timeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1000")
    })
    public String timeout(@PathVariable("id")Integer id) {
        return server.infoTimeOut(id);
    }


    @GetMapping("/timeout/{id}")
    // 不具体指定降级配置
    @HystrixCommand
    public String timeout2(@PathVariable("id")Integer id) {
        return server.infoTimeOut(id);
    }

    /**
     * 特定的方法的服务降级
     */
    public String timeoutHandler(@PathVariable("id")Integer id) {
        return "消费端调用超时，请稍后再试！" + id;
    }
    
    /**
     * 全局的降级处理
     */
    public String globalTimeout() {
        return "全局降级！";
    }
}
```

### 指定服务降级处理类

因为一般不会在controller里进行处理异常情况，所以我们可以直接在业务接口处进行处理降级。

```Java
// 指定服务降级的处理类
@FeignClient(value = "HYSTRIX-PROVIDER-PAYMENT",fallback = FeignConsumerImpl.class)
public interface FeignConsumer {

    /**
     * 正常访问
     * @param id    ID
     * @return      响应
     */
    @GetMapping("/payment/ok/{id}")
    String infoOk(@PathVariable("id")Integer id);

    /**
     * 超时访问
     * @param id    ID
     * @return      响应
     */
    @GetMapping("/payment/timeout/{id}")
    String infoTimeOut(@PathVariable("id")Integer id);

}
```

```Java
/**
 * <p>
 *     服务降级处理类
 * 	首先先实现业务接口，然后处理降级情况。
 * </p>
 *
 * @author yww
 **/
@Component
public class FeignConsumerImpl implements FeignConsumer{
    @Override
    public String infoOk(Integer id) {
        return "系统繁忙，请稍后再试!";
    }

    @Override
    public String infoTimeOut(Integer id) {
        return "系统繁忙，请稍后再试!";
    }
}
```

> 注意这两种降级的方法还是有点区别的。
>
> 在业务接口的降级处理其实是配置Feign的降级，也就是服务提供端出现问题的处理，在controller的降级处理其实算是在服务消费者的降级处理。

### 服务熔断

服务熔断是一种保护机制，当扇出链路的某个微服务出错不可用或者响应时间太长，会进行服务的降级，进而熔断该节点微服务的调用，快速返回错误的响应信息，当检测到该节点微服务调用响应正常后，恢复调用链路。

简单的说，就是当一段时间内失败的调用达到指定的阈值后，就会触发熔断机制。

下面配置一下服务端的服务熔断。

在业务接口类配置服务熔断设置。

```Java
@Service
public class PaymentService {

    /**
     *  服务熔断
     *  circuitBreaker.enabled                    	是否开启断路器
     *  circuitBreaker.requestVolumeThreshold       请求的次数，默认20个请求
     *  circuitBreaker.sleepWindowInMilliseconds    时间窗口期，默认十秒
     *  circuitBreaker.errorThresholdPercentage     请求失败率的峰值，默认50%失败率
     *  配置组合的意义
     *  在10000ms内，不足20次，不会打开断路器
     *	在10000ms内，超过了20次，若是失败率达到了50%就会触发断路器	
     */
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
        if (id < 0) {
            throw new RuntimeException();
        }
        String number = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + "\t" + "调用成功， UUID为" + number;
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id") Integer id) {
        return "id 不能为负数，请稍后再试！ 当前ID为：" + id;
    }
    
}
```

> 断路器开启之后，请求就不会调用主逻辑了，即不用判断请求正确还是错误，直接调用fallback，直接服务降级。
>
> 一段时间（默认是5秒），断路器是半开状态，会让其中一个请求进行转发，如果成功，断路器关闭，若失败，则继续开启
>
> 熔断类型。
>
> 1. Open。请求不再调用当前服务，直接进行服务降级处理，打开时间达到设置时间，进入半熔断状态。
> 2. Half Open。部分请求根据规则调用当前服务，若是请求成功并且符合规则则认为服务恢复正常，关闭熔断。
> 3. Closed。不对服务进行熔断处理。

### 服务熔断所有的配置

```Java
@HystrixCommand(fallbackMethod = "fallbackMethod", 
                groupKey = "strGroupCommand", 
                commandKey = "strCommand", 
                threadPoolKey = "strThreadPool",
                commandProperties = {
                    // 设置隔离策略，THREAD 表示线程池 SEMAPHORE：信号池隔离
                    @HystrixProperty(name = "execution.isolation.strategy", value = "THREAD"),
                    // 当隔离策略选择信号池隔离的时候，用来设置信号池的大小（最大并发数）
                    @HystrixProperty(name = "execution.isolation.semaphore.maxConcurrentRequests", value = "10"),
                    // 配置命令执行的超时时间
                    @HystrixProperty(name = "execution.isolation.thread.timeoutinMilliseconds", value = "10"),
                    // 是否启用超时时间
                    @HystrixProperty(name = "execution.timeout.enabled", value = "true"),
                    // 执行超时的时候是否中断
                    @HystrixProperty(name = "execution.isolation.thread.interruptOnTimeout", value = "true"),
                    // 执行被取消的时候是否中断
                    @HystrixProperty(name = "execution.isolation.thread.interruptOnCancel", value = "true"),
                    // 允许回调方法执行的最大并发数
                    @HystrixProperty(name = "fallback.isolation.semaphore.maxConcurrentRequests", value = "10"),
                    // 服务降级是否启用，是否执行回调函数
                    @HystrixProperty(name = "fallback.enabled", value = "true"),
                    // 是否启用断路器
                    @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
                    // 该属性用来设置在滚动时间窗中，断路器熔断的最小请求数。例如，默认该值为 20 的时候，如果滚动时间窗（默认10秒）内仅收到了19个请求， 即使这19个请求都失败了，断路器也不会打开。
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "20"),
                    // 该属性用来设置在滚动时间窗中，表示在滚动时间窗中，在请求数量超过 circuitBreaker.requestVolumeThreshold 的情况下，如果错误请求数的百分比超过50, 就把断路器设置为 "打开" 状态，否则就设置为 "关闭" 状态。
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    // 该属性用来设置当断路器打开之后的休眠时间窗。 休眠时间窗结束之后，会将断路器置为 "半开" 状态，尝试熔断的请求命令，如果依然失败就将断路器继续设置为 "打开" 状态，如果成功就设置为 "关闭" 状态。
                    @HystrixProperty(name = "circuitBreaker.sleepWindowinMilliseconds", value = "5000"),
                    // 断路器强制打开
                    @HystrixProperty(name = "circuitBreaker.forceOpen", value = "false"),
                    // 断路器强制关闭
                    @HystrixProperty(name = "circuitBreaker.forceClosed", value = "false"),
                    // 滚动时间窗设置，该时间用于断路器判断健康度时需要收集信息的持续时间
                    @HystrixProperty(name = "metrics.rollingStats.timeinMilliseconds", value = "10000"),
                    // 该属性用来设置滚动时间窗统计指标信息时划分"桶"的数量，断路器在收集指标信息的时候会根据设置的时间窗长度拆分成多个 "桶" 来累计各度量值，每个"桶"记录了一段时间内的采集指标。
                    // 比如 10 秒内拆分成 10 个"桶"收集这样，所以 timeinMilliseconds 必须能被 numBuckets 整除。否则会抛异常
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "10"),
                    // 该属性用来设置对命令执行的延迟是否使用百分位数来跟踪和计算。如果设置为 false, 那么所有的概要统计都将返回 -1。
                    @HystrixProperty(name = "metrics.rollingPercentile.enabled", value = "false"),
                    // 该属性用来设置百分位统计的滚动窗口的持续时间，单位为毫秒。
                    @HystrixProperty(name = "metrics.rollingPercentile.timeInMilliseconds", value = "60000"),
                    // 该属性用来设置百分位统计滚动窗口中使用 “ 桶 ”的数量。
                    @HystrixProperty(name = "metrics.rollingPercentile.numBuckets", value = "60000"),
                    // 该属性用来设置在执行过程中每个 “桶” 中保留的最大执行次数。如果在滚动时间窗内发生超过该设定值的执行次数，
                    // 就从最初的位置开始重写。例如，将该值设置为100, 滚动窗口为10秒，若在10秒内一个 “桶 ”中发生了500次执行，
                    // 那么该 “桶” 中只保留 最后的100次执行的统计。另外，增加该值的大小将会增加内存量的消耗，并增加排序百分位数所需的计算时间。
                    @HystrixProperty(name = "metrics.rollingPercentile.bucketSize", value = "100"),
                    
                    // 该属性用来设置采集影响断路器状态的健康快照（请求的成功、 错误百分比）的间隔等待时间。
                    @HystrixProperty(name = "metrics.healthSnapshot.intervalinMilliseconds", value = "500"),
                    // 是否开启请求缓存
                    @HystrixProperty(name = "requestCache.enabled", value = "true"),
                    // HystrixCommand的执行和事件是否打印日志到 HystrixRequestLog 中
                    @HystrixProperty(name = "requestLog.enabled", value = "true"),
                },
                threadPoolProperties = {
                    // 该参数用来设置执行命令线程池的核心线程数，该值也就是命令执行的最大并发量
                    @HystrixProperty(name = "coreSize", value = "10"),
                    // 该参数用来设置线程池的最大队列大小。当设置为 -1 时，线程池将使用 SynchronousQueue 实现的队列，否则将使用 LinkedBlockingQueue 实现的队列。
                    @HystrixProperty(name = "maxQueueSize", value = "-1"),
                    // 该参数用来为队列设置拒绝阈值。 通过该参数， 即使队列没有达到最大值也能拒绝请求。
                    // 该参数主要是对 LinkedBlockingQueue 队列的补充,因为 LinkedBlockingQueue 队列不能动态修改它的对象大小，而通过该属性就可以调整拒绝请求的队列大小了。
                    @HystrixProperty(name = "queueSizeRejectionThreshold", value = "5"),
                }
               )
public String doThing() {
	...
}

```

### HystrixDashboard

HystrixDashboard准实时的调用监控，持续的通过记录所有通过Hystrix发起的请求的执行信息。

引入依赖。

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
```

主启动类开启支持。

```java
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashBoard {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDashBoard.class, args);
    }
}
```

然后启动就可以访问到页面了。<http://localhost:9001/hystrix>

被监控的服务也要设置一下。

```Java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class HystrixPaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixPaymentMain.class, args);
    }

   /**
     * 此配置是为了服务监控而配置
     */
    @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```

然后在<http://localhost:9001/hystrix>填写监控的地址。

<http://localhost:8003/hystrix.stream>

# 服务网关

## Zuul

暂不考虑。

## SpringCloudGateway

 SpringCloudGateway新一代的网关，主要当作为服务的入口。

### 重要的概念

1. 路由（Route）

   路由是构建网关的基本模块，由ID，目标URI，一系列的断言和过滤器组成，如果断言为true则匹配该路由

2. 断言（Predicate）

   参考了Java8的一个重要的函数接口`Predicate`，开发人员可以匹配HTTP请求中的所有内容，如果请求与断言相匹配则进行路由

3. 过滤（Filter）

   使用过滤器，可以在请求被路由前或者请求之后对请求进行修改

### 简单的使用

引入依赖。

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```

编写配置文件。

```yml
server:
  port: 9527

spring:
  application:
    name: cloud-gateway

eureka:
  instance:
    hostname: cloud-gateway
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka
```

主启动类。

```Java
@SpringBootApplication
@EnableEurekaClient
public class GatewayMain {
    public static void main(String[] args) {
        SpringApplication.run(GatewayMain.class, args);
    }
}
```

网关的配置主要是在配置配置。

```

```
