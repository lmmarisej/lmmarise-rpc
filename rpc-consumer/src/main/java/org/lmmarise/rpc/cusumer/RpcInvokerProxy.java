package org.lmmarise.rpc.cusumer;

import io.netty.channel.DefaultEventLoop;
import io.netty.util.concurrent.DefaultPromise;
import org.lmmarise.rpc.common.RpcFuture;
import org.lmmarise.rpc.common.RpcRequest;
import org.lmmarise.rpc.common.RpcRequestHolder;
import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.protocol.MsgHeader;
import org.lmmarise.rpc.protocol.MsgType;
import org.lmmarise.rpc.protocol.ProtocolConstants;
import org.lmmarise.rpc.protocol.RpcProtocol;
import org.lmmarise.rpc.provider.registry.RegistryService;
import org.lmmarise.rpc.serialization.SerializationTypeEnum;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 方法代理处理器，负责处理客户端进行的远程调用，屏蔽远程访问的细节。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 16:23
 */
public class RpcInvokerProxy implements InvocationHandler {

    private final String serviceVersion;
    private final long timeout;
    private final RegistryService registryService;

    public RpcInvokerProxy(String serviceVersion, long timeout, RegistryService registryService) {
        this.serviceVersion = serviceVersion;
        this.timeout = timeout;
        this.registryService = registryService;
    }

    /**
     * 将本地调用转为远程调用。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构造一个 RPC 请求报文
        RpcProtocol<RpcRequest> protocol = new RpcProtocol<>();

        // 构造报文头
        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();     // 本次请求的唯一 id
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.REQUEST.getType());
        header.setStatus((byte) 0x1);

        protocol.setHeader(header);

        // 构造请求体
        RpcRequest request = new RpcRequest();
        request.setServiceVersion(this.serviceVersion);
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParams(args);

        // 报文构造完成
        protocol.setBody(request);

        RpcFuture<RpcResponse> future = new RpcFuture<>(new DefaultPromise<>(new DefaultEventLoop()), timeout);
        RpcRequestHolder.REQUEST_MAP.put(requestId, future);   // 将本次请求在本地的存根，响应结果由 RpcResponseHandler 接收并写入

        RpcConsumer.INSTANCE.sendRequest(protocol, this.registryService);   // 启动本地网络服务，发起 RPC 请求

        return future.getPromise();     // todo 增加扩展性
    }
}
