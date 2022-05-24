package org.lmmarise.rpc.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RPC 配置信息，通过配置文件装配为 bean。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:33
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {
    private int servicePort;
    private String registryAddress;
    private String registryType;
}
