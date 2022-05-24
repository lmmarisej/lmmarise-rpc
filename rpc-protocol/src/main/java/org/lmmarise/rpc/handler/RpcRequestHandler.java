package org.lmmarise.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.common.RpcFuture;
import org.lmmarise.rpc.common.RpcRequest;
import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.common.RpcServiceHelper;
import org.lmmarise.rpc.protocol.MsgHeader;
import org.lmmarise.rpc.protocol.MsgStatus;
import org.lmmarise.rpc.protocol.MsgType;
import org.lmmarise.rpc.protocol.RpcProtocol;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * 请求处理器，基于 Netty 处理来自客户端的 RPC 请求报文。
 * <p>
 * 根据客户端发出的 RPC 请求信息，调用本地方法执行，响应执行结果。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 01:35
 */
@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcRequest>> {

    private final Map<String, Object> rpcServiceMap;        // 记录对外暴露的服务，客户端可以的远程服务都汇聚于此

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }

    /**
     * 将业务处理和请求响应分发给线程池处理，I/O 线程直接返回。
     *
     * @param ctx 由于 ChannelHandler 可以添加到多个 ChannelPipeline，
     *            ChannelHandlerContext 在 ChannelHandler 添加到 ChannelPipeline 时创建，
     *            ChannelHandler 便能通过 ctx 在不同的 Pipeline 中保存不同的状态。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcRequest> protocol) throws Exception {
        // 异步处理，避免阻塞 Netty I/O 线程
        RpcRequestProcessor.submitRequest(() -> {
            RpcProtocol<RpcResponse> resProtocol = new RpcProtocol<>();     // 构造一个报文，用于响应。报文由头、负载构成
            RpcResponse response = new RpcResponse();       // 构造响应报文-负载
            MsgHeader header = protocol.getHeader();        // 请求报文-头，后面转为响应报文-头来使用，少量字段需要修改
            header.setMsgType((byte) MsgType.RESPONSE.getType());   // 响应报文-头-类型
            try {
                Object result = handle(protocol.getBody());     // 根据请求报文-负载中请求的方法名等信息，反射调用本地服务
                response.setData(result);                       // 调用本地服务的返回值作为响应报文-负载-数据

                header.setStatus((byte) MsgStatus.SUCCESS.getCode());   // 响应报文-头-状态
                resProtocol.setHeader(header);      // 响应报文-头
                resProtocol.setBody(response);      // 响应报文-负载
            } catch (Throwable throwable) {
                header.setStatus((byte) MsgStatus.FAIL.getCode());  // 响应报文-头-状态
                response.setMessage(throwable.toString());          // 响应报文-负载-失败信息
                log.error("process request {} error", header.getRequestId(), throwable);
            }
            ctx.writeAndFlush(resProtocol);     // 将响应报文写入缓冲区并发送
        });
    }

    /**
     * 处理请求。
     */
    private Object handle(RpcRequest request) throws Throwable {        // 显示抛出 Throwable，强制编译检查
        // 获取被请求的 bean
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();     // 提供服务的类

        // 获取被请求的 bean 方法，以及调用者提供的方法参数
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        FastClass fastClass = FastClass.create(serviceClass);       // 生成子类
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);       // 定位方法

        Object result = fastClass.invoke(methodIndex, serviceBean, parameters);// 基于委托进调用，不使用反射调用，也就能避开大量反射检查

        // todo 异步调用必须设置超时时间

        // 返回类型是 Future，必须先将 Future 中的值取出来再返回
        if (result instanceof Future) {
            return ((Future<?>) result).get();                      // 阻塞地取值
        } else if (result instanceof RpcFuture) {
            return ((RpcFuture<?>) result).getPromise().get();      // 阻塞地取值
        }
        // 直接返回
        else {
            return result;
        }
    }
}
