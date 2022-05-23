package org.lmmarise.rpc.provider.registry;

import org.lmmarise.rpc.common.ServiceMeta;

import java.io.IOException;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:58
 */
public class EurekaRegistryService implements RegistryService {
    public EurekaRegistryService(String registryAddr) {
    }

    @Override
    public void register(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public void unRegister(ServiceMeta serviceMeta) throws Exception {

    }

    @Override
    public ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception {
        return null;
    }

    @Override
    public void destroy() throws IOException {

    }
}
