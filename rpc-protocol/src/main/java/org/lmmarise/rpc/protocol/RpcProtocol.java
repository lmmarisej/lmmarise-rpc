package org.lmmarise.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * 对应用层 RPC 传输协议的抽象。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:48
 */
@Data
public class RpcProtocol<T> implements Serializable {
    private MsgHeader header;   // 协议头
    private T body;             // 协议体
}
