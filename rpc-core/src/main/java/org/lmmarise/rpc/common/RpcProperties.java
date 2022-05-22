package org.lmmarise.rpc.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:33
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcProperties {
    private int servicePort;
    private String registryAddr;
    private String registryType;
}
