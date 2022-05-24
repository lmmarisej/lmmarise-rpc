package org.lmmarise.rpc.provider;

import org.lmmarise.rpc.common.RpcProperties;
import org.lmmarise.rpc.provider.registry.RegistryFactory;
import org.lmmarise.rpc.provider.registry.RegistryService;
import org.lmmarise.rpc.provider.registry.RegistryType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 服务提供者自动配置。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 21:04
 */
@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class RpcProviderAutoConfiguration {

    @Resource
    private RpcProperties rpcProperties;

    @Bean
    public RpcProvider init() throws Exception {
        RegistryType type = RegistryType.valueOf(rpcProperties.getRegistryType());
        RegistryService serviceRegistry = RegistryFactory.getInstance(rpcProperties.getRegistryAddress(), type);
        return new RpcProvider(rpcProperties.getServicePort(), serviceRegistry);
    }
}
