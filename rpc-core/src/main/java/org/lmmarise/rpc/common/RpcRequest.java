package org.lmmarise.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 一个 RPC 请求报文体，用于抽象被调用的方法。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:27
 */
@Data
public class RpcRequest implements Serializable {
    private String serviceVersion;  // 服务版本号，类级别
    private String className;
    private String methodName;
    private Object[] params;
    private Class<?>[] parameterTypes;
}
