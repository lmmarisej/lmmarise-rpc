package org.lmmarise.rpc.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 维护所有 RPC 请求存根。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:43
 */
public class RpcRequestHolder {

    // 请求 ID 生成器
    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    // 一个  ID 与 一个 RPC 请求存根对应
    public static final ConcurrentMap<Long, RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();

}
