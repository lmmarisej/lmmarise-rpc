package org.lmmarise.rpc.facade;

import org.lmmarise.rpc.common.RpcFuture;
import org.lmmarise.rpc.common.RpcResponse;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:10
 */
public interface HelloFacade {

    String hello(String name);

    RpcFuture<RpcResponse> homework(String thing);

}
