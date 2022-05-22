package org.lmmarise.rpc.common;

import lombok.Data;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:39
 */
@Data
public class ServiceMeta {
    private String serviceName;
    private String serviceVersion;
    private String serviceAddr;
    private int servicePort;
}
