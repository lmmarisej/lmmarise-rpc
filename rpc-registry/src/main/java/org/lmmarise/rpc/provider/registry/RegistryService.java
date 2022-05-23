package org.lmmarise.rpc.provider.registry;

import org.lmmarise.rpc.common.ServiceMeta;

import java.io.IOException;

/**
 * 服务注册与发现功能的抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:48
 */
public interface RegistryService {

    void register(ServiceMeta serviceMeta) throws Exception;

    void unRegister(ServiceMeta serviceMeta) throws Exception;

    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    void destroy() throws IOException;

}
