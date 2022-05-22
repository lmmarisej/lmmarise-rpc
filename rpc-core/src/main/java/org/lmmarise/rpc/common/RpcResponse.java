package org.lmmarise.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 一个 RPC 响应的抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:44
 */
@Data
public class RpcResponse implements Serializable {
    // 响应体
    private Object data;
    // 提示类型信息，比如错误信息
    private String message;
}
