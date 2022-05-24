package org.lmmarise.rpc.common;

import io.netty.util.concurrent.Promise;
import lombok.Data;

/**
 * 一个 RPC 请求的本地存根，用于后续获得远程服务的响应值。
 *
 * @param <T>
 * @author lmmarise.j@gmail.com
 * @since 2022/5/22 21:27
 */
@Data
public class RpcFuture<T> {
    private Promise<T> promise;     // 用于异步调用结果的获取与写入
    private long timeout;           // 请求超时时间
    private Class<?> returnType;    // 返回值类型

    public RpcFuture() {
    }

    public RpcFuture(Promise<T> promise, long timeout, Class<?> returnType) {
        this.promise = promise;
        this.timeout = timeout;
        this.returnType = returnType;
    }
}
