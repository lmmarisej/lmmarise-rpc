package org.lmmarise.rpc.provider.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.lmmarise.rpc.common.RpcServiceHelper;
import org.lmmarise.rpc.common.ServiceMeta;
import org.lmmarise.rpc.provider.registry.loadbalancer.ZKConsistentHashLoadBalancer;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 通过 Zookeeper 实现注册与发现。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:57
 */
public class ZookeeperRegistryService implements RegistryService {
    public static final int BASE_SLEEP_TIME_MS = 1000;
    public static final int MAX_RETRIES = 3;
    public static final String ZK_BASE_PATH = "/lmmarise_rpc";

    private final ServiceDiscovery<ServiceMeta> serviceDiscovery;           // 由 ServiceDiscovery 完成服务的注册和发现

    public ZookeeperRegistryService(String registryAddress) throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                registryAddress,
                new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES)
        );

        client.start();     // 创建完 CuratorFramework 实例之后需要调用 start() 进行启动

        JsonInstanceSerializer<ServiceMeta> serializer = new JsonInstanceSerializer<>(ServiceMeta.class);

        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMeta.class)
                .client(client)
                .serializer(serializer)     // 与 Zookeeper 通信时使用的协议
                .basePath(ZK_BASE_PATH)
                .build();

        this.serviceDiscovery.start();
    }

    /**
     * 服务端：将服务元数据信息 ServiceMeta 发布到注册中心。
     */
    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance.<ServiceMeta>builder()
                // 将 serviceMeta 注册到在 Zookeeper 中的该 key 上，在 Zookeeper 中一个 key 可以有多个 value，方便后续负载均衡
                .name(RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()))
                .address(serviceMeta.getServiceAddress())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.registerService(serviceInstance);
    }

    /**
     * 服务端：将 Zookeeper 端的该 ServiceMeta 实例删除。
     */
    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {
        ServiceInstance<ServiceMeta> serviceInstance = ServiceInstance
                .<ServiceMeta>builder()
                .name(serviceMeta.getServiceName())
                .address(serviceMeta.getServiceAddress())
                .port(serviceMeta.getServicePort())
                .payload(serviceMeta)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    /**
     * 客户端：根据调用方法，找到服务端注册的 ServiceMeta list。通过本地负载均衡算法选择其中一个进行调用。
     */
    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        Collection<ServiceInstance<ServiceMeta>> serviceInstances = serviceDiscovery.queryForInstances(serviceName);
        ServiceInstance<ServiceMeta> instance = new ZKConsistentHashLoadBalancer().select(
                (List<ServiceInstance<ServiceMeta>>) serviceInstances,
                invokerHashCode
        );
        if (instance != null) {
            return instance.getPayload();
        }
        return null;
    }

    /**
     * 在系统退出的时候需要将初始化的实例进行关闭。
     */
    @Override
    public void destroy() throws IOException {
        serviceDiscovery.close();
    }
}
