package org.lmmarise.rpc.provider.registry;

/**
 * 工厂模式，根据不同的注册中心提供不同的服务注册与发现的接口实例化。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:52
 */
public class RegistryFactory {

    private static volatile RegistryService registryService;

    public static RegistryService getInstance(String registryAddr, RegistryType type) throws Exception {
        if (registryService == null) {
            synchronized (RegistryFactory.class) {
                if (registryService == null) {
                    switch (type) {
                        case ZOOKEEPER:
                            registryService = new ZookeeperRegistryService(registryAddr);
                            break;
                        case EUREKA:
                            registryService = new EurekaRegistryService(registryAddr);
                            break;
                    }
                }
            }
        }
        return registryService;
    }
}
