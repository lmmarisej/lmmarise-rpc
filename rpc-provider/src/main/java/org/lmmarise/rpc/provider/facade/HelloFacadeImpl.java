package org.lmmarise.rpc.provider.facade;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import org.lmmarise.rpc.common.RpcFuture;
import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.facade.HelloFacade;
import org.lmmarise.rpc.provider.annotation.RpcService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:12
 */
@RpcService(serviceInterface = HelloFacade.class, serviceVersion = "1.0.0")
public class HelloFacadeImpl implements HelloFacade {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public String hello(String name) {
        return "hello '" + name + "'.";
    }

    @Override
    public RpcFuture<RpcResponse> homework(String thing) {
        RpcFuture<RpcResponse> rpcFuture = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), 3000);
        RpcResponse rpcResponse = new RpcResponse();
        executor.submit(() -> {
            try {
                Thread.sleep(2000);
                rpcResponse.setData("鸡你太美");
                rpcFuture.getPromise().setSuccess(rpcResponse);
            } catch (InterruptedException e) {
                rpcResponse.setMessage(e.getMessage());
            }
        });
        return rpcFuture;
    }
}
