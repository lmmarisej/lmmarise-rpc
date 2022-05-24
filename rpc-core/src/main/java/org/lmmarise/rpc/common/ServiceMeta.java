package org.lmmarise.rpc.common;

import lombok.Data;

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
}
