package org.lmmarise.rpc.common;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 一个 RPC 响应报文的响应体，用于抽象被调用的方法返回值。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:44
 */
@Data
@Accessors(chain = true)
public class RpcResponse implements Serializable {
    // 响应体
    private Object data;
    // 提示类型信息，比如错误信息
    private String message;

    public RpcResponse() {
    }
}
