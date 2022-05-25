package org.lmmarise.rpc.common;

import lombok.Data;

import java.util.Objects;

/**
 * 对远程服务信息的抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:39
 */
@Data
public class ServiceMeta {
    private String serviceName;
    private String serviceVersion;
    private String serviceAddress;
    private int servicePort;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta that = (ServiceMeta) o;
        return servicePort == that.servicePort
                && Objects.equals(serviceName, that.serviceName)
                && Objects.equals(serviceVersion, that.serviceVersion)
                && Objects.equals(serviceAddress, that.serviceAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceVersion, serviceAddress, servicePort);
    }
}
