package org.lmmarise.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.lmmarise.rpc.common.RpcFuture;
import org.lmmarise.rpc.common.RpcRequestHolder;
import org.lmmarise.rpc.common.RpcResponse;
import org.lmmarise.rpc.protocol.RpcProtocol;

/**
 * 请求处理器，基于 Netty 处理来自服务端的 RPC 响应报文。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 14:03
 */
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcProtocol<RpcResponse>> {

    /**
     * 接收响应，根据响应 id 将响应结果写入对应 Promise。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol<RpcResponse> msg) {
        long requestId = msg.getHeader().getRequestId();
        RpcFuture<RpcResponse> future = RpcRequestHolder.REQUEST_MAP.remove(requestId);     // 根据响应请求 id，获取本地存根
        future.getPromise().setSuccess(msg.getBody());      // 接收到服务端响应数据，写入Promise，唤醒消费者线程
    }

}
