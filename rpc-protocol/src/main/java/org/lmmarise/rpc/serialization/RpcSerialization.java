package org.lmmarise.rpc.serialization;

import org.lmmarise.rpc.protocol.MsgHeader;

import java.io.IOException;

/**
 * 序列化与反序列化接口。
 *
 * @see MsgHeader#getSerialization()   通过 RPC 协议指定报文使用的具体的序列化协议。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 00:22
 */
public interface RpcSerialization {

    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;

}