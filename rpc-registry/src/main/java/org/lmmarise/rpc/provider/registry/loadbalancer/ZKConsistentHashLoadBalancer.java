package org.lmmarise.rpc.provider.registry.loadbalancer;

import org.apache.curator.x.discovery.ServiceInstance;
import org.lmmarise.rpc.common.ServiceMeta;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 基于 Zookeeper 的负载均衡。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 19:27
 */
public class ZKConsistentHashLoadBalancer implements ServiceLoadBalancer<ServiceInstance<ServiceMeta>> {
    private final static int VIRTUAL_NODE_SIZE = 10;
    private final static String VIRTUAL_NODE_SPLIT = "#";

    /**
     * 从虚拟的 hash 环上选择一个节点。
     */
    @Override
    public ServiceInstance<ServiceMeta> select(List<ServiceInstance<ServiceMeta>> servers, int hashCode) {
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = makeConsistentHashRing(servers);
        return allocateNode(ring, hashCode);
    }

    /**
     * 构造一个虚拟的 hash 环。
     */
    private TreeMap<Integer, ServiceInstance<ServiceMeta>> makeConsistentHashRing(List<ServiceInstance<ServiceMeta>> servers) {
        TreeMap<Integer, ServiceInstance<ServiceMeta>> ring = new TreeMap<>();
        for (ServiceInstance<ServiceMeta> instance : servers) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                ring.put((buildServiceInstanceKey(instance) + VIRTUAL_NODE_SPLIT + i).hashCode(), instance);
            }
        }
        return ring;
    }

    /**
     * 取出 hash 环上最接近指定 hashcode 的节点。
     */
    private ServiceInstance<ServiceMeta> allocateNode(TreeMap<Integer, ServiceInstance<ServiceMeta>> ring, int hashCode) {
        Map.Entry<Integer, ServiceInstance<ServiceMeta>> entry = ring.ceilingEntry(hashCode);
        if (entry == null) {
            entry = ring.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * 通过 ServiceMeta 的 `地址 + : + 端口` 构造一个 key。
     */
    private String buildServiceInstanceKey(ServiceInstance<ServiceMeta> instance) {
        ServiceMeta payload = instance.getPayload();
        return String.join(":", payload.getServiceAddress(), String.valueOf(payload.getServicePort()));
    }
}
