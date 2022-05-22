package org.lmmarise.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 一个 RPC 请求的抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:27
 */
@Data
public class RpcRequest implements Serializable {
    private String serviceVersion;
    private String className;
    private String methodName;
    private Object[] params;
    private Class<?>[] parameterTypes;
}
