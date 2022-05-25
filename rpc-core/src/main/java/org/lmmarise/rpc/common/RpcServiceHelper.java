package org.lmmarise.rpc.common;

/**
 * 远程服务辅助工具。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:36
 */
public class RpcServiceHelper {

    private static final String VERSION_SEPARATOR = "#";
    private static final String PORT_SEPARATOR = ":";

    /**
     * 服务注册到注册中心的服务名会带上版本号作为标记。
     * <p>
     * 例如："name" : "rpc.provider.facade.HelloFacade#1.0.0"
     */
    public static String buildServiceKey(String serviceName, String serviceVersion) {
        return String.join(VERSION_SEPARATOR, serviceName, serviceVersion);
    }

    public static String[] deBuildServiceKey(String serviceKey) {
        return serviceKey.split(VERSION_SEPARATOR);
    }

    public static String buildAddressPortKey(String address, String port) {
        return String.join(PORT_SEPARATOR, address, port);
    }

    public static String buildAddressPortKey(String address, int port) {
        return buildAddressPortKey(address, Integer.toString(port));
    }

    public static String[] deBuildAddressPortKey(String addressPortKey) {
        return addressPortKey.split(PORT_SEPARATOR);
    }
}
