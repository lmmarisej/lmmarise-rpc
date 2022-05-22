package org.lmmarise.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * Data packet，RPC 应用层报文，报文分为头、负载。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 22:48
 */
@Data
public class RpcProtocol<T> implements Serializable {
    /**
     * 包头，响应或请求等包头格式都一样，部分数据不一致。
     */
    private MsgHeader header;

    /**
     * 负载，响应或请求等，数据可能都不一致。
     */
    private T body;
}
