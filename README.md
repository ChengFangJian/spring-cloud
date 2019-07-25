# spring-cloud
spring cloud：Apache旗下的Spring体系下的微服务解决方案。SpringCloud是关注全局的微服务协调整理治理框架,整合并管理各个微服务,为各个微服务之间提供,配置管理,服务发现,断路器,路由,事件总线等集成服务。

# Eureka
**Register：服务注册**

当Eureka客户端向Eureka Server注册时，它提供自身的元数据，比如IP地址、端口，运行状况指示符URL，主页等。

**Renew：服务续约**

Eureka客户会每隔30秒发送一次心跳来续约。 通过续约来告知Eureka Server该Eureka客户仍然存在，没有出现问题。 正常情况下，如果Eureka Server在90秒没有收到Eureka客户的续约，它会将实例从其注册表中删除。 建议不要更改续约间隔。

Fetch Registries：获取注册列表信息

Eureka客户端从服务器获取注册表信息，并将其缓存在本地。客户端会使用该信息查找其他服务，从而进行远程调用。该注册列表信息定期（每30秒钟）更新一次。每次返回注册列表信息可能与Eureka客户端的缓存信息不同， Eureka客户端自动处理。如果由于某种原因导致注册列表信息不能及时匹配，Eureka客户端则会重新获取整个注册表信息。 Eureka服务器缓存注册列表信息，整个注册表以及每个应用程序的信息进行了压缩，压缩内容和没有压缩的内容完全相同。Eureka客户端和Eureka 服务器可以使用JSON / XML格式进行通讯。在默认的情况下Eureka客户端使用压缩JSON格式来获取注册列表的信息。

**Cancel：服务下线**

Eureka客户端在程序关闭时向Eureka服务器发送取消请求。 发送请求后，该客户端实例信息将从服务器的实例注册表中删除。该下线请求不会自动完成，它需要调用以下内容：
DiscoveryManager.getInstance().shutdownComponent()；

**Eviction 服务剔除**

在默认的情况下，当Eureka客户端连续90秒没有向Eureka服务器发送服务续约，即心跳，Eureka服务器会将该服务实例从服务注册列表删除，即服务剔除。

**高并发：**

1.纯内存维护注册表，CocurrentHashMap数据结构

2.多级缓存。

在拉取注册表的时候：

首先从ReadOnlyCacheMap里查缓存的注册表。

若没有，就找ReadWriteCacheMap里缓存的注册表。

如果还没有，就从内存中获取实际的注册表数据。

在注册表发生变更的时候：
    
会在内存中更新变更的注册表数据，同时过期掉ReadWriteCacheMap。

此过程不会影响ReadOnlyCacheMap提供人家查询注册表。

一段时间内（默认30秒），各服务拉取注册表会直接读ReadOnlyCacheMap

30秒过后，Eureka Server的后台线程发现ReadWriteCacheMap已经清空了，也会清空ReadOnlyCacheMap中的缓存

下次有服务拉取注册表，又会从内存中获取最新的数据了，同时填充各个缓存。

# Ribbon
Ribbon是Netflix公司开源的一个负载均衡的项目，它属于上述的第二种，是一个客户端负载均衡器，运行在客户端上。它是一个经过了云端测试的IPC库，可以很好地控制HTTP和TCP客户端的一些行为。 Feign已经默认使用了Ribbon。

主要通过LoadBalancerClient来实现的，而LoadBalancerClient具体交给了ILoadBalancer来处理，ILoadBalancer通过配置IRule、IPing等信息，并向EurekaClient获取注册列表的信息，并默认10秒一次向EurekaClient发送“ping”,进而检查是否更新服务列表，最后，得到注册列表后，ILoadBalancer根据IRule的策略进行负载均衡。

而RestTemplate 被@LoadBalance注解后，能过用负载均衡，主要是维护了一个被@LoadBalance注解的RestTemplate列表，并给列表中的RestTemplate添加拦截器，进而交给负载均衡器去处理。

# Hystrix
在分布式系统中，服务与服务之间依赖错综复杂，一种不可避免的情况就是某些服务将会出现失败。Hystrix是一个库，它提供了服务与服务之间的容错功能，主要体现在延迟容错和容错，从而做到控制分布式系统中的联动故障。Hystrix通过隔离服务的访问点，阻止联动故障，并提供故障的解决方案，从而提高了这个分布式系统的弹性。

# Feign
1.首先通过@EnableFeignCleints注解开启FeignCleint

2.根据Feign的规则实现接口，并加@FeignCleint注解

3.程序启动后，会进行包扫描，扫描所有的@ FeignCleint的注解的类，并将这些信息注入到ioc容器中。

4.当接口的方法被调用，通过jdk的代理，来生成具体的RequesTemplate

5.RequesTemplate再生成Request

6.Request交给Client去处理，其中Client可以是HttpUrlConnection、HttpClient也可以是Okhttp

7.最后Client被封装到LoadBalanceClient类，这个类结合类Ribbon做到了负载均衡。

# zuul
1.首先将请求给zuulservlet处理

2.zuulservlet中有一个zuulRunner对象

3.该对象中初始化了RequestContext：作为存储整个请求的一些数据，并被所有的zuulfilter共享。

4.zuulRunner中还有 FilterProcessor，FilterProcessor作为执行所有的zuulfilter的管理器。

5.FilterProcessor从filterloader 中获取zuulfilter

6.而zuulfilter是被filterFileManager所加载，并支持groovy热加载，采用了轮询的方式热加载。有了这些filter之后，zuulservelet首先执行的Pre类型的过滤器，再执行route类型的过滤器，最后执行的是post 类型的过滤器，如果在执行这些过滤器有错误的时候则会执行error类型的过滤器。

7.执行完这些过滤器，最终将请求的结果返回给客户端。
