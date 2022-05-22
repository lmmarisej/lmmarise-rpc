package org.lmmarise.rpc.common;

/**
 * 远程服务辅助工具。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:36
 */
public class RpcServiceHelper {

    /**
     * 服务注册到注册中心的服务名会带上版本号作为标记。
     *
     * 例如："name" : "rpc.provider.facade.HelloFacade#1.0.0"
     */
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.join("#", serviceName, serviceVersion);
    }
}
