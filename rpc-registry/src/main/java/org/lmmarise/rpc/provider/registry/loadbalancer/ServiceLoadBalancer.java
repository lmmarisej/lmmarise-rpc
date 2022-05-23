package org.lmmarise.rpc.provider.registry.loadbalancer;

import java.util.List;

/**
 * 负载均衡功能抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 19:24
 */
public interface ServiceLoadBalancer<T> {

    T select(List<T> servers, int hashCode);

}