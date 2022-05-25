package org.lmmarise.rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.lmmarise.rpc.common.RpcRequestHolder;
import org.lmmarise.rpc.protocol.*;
import org.lmmarise.rpc.serialization.SerializationTypeEnum;

import java.util.concurrent.TimeUnit;

/**
 * 客户端定时向服务端发送心跳，用于暂停服务端关闭空闲连接的定时器 {@link RpcIdleStateHandler} )}。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/25 15:05
 */
public class RpcHeartBeatHandler extends ChannelInboundHandlerAdapter {

    /**
     * 客户端连接后触发。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        doHeartBeatTask(ctx);
    }

    /**
     * 采用 EventLoop 提供的 schedule() 方法向任务队列中添加心跳数据上报的定时任务。
     */
    private void doHeartBeatTask(ChannelHandlerContext ctx) {
        ctx.executor().schedule(() -> {
            if (ctx.channel().isActive()) {
                ctx.writeAndFlush(buildHeartBeatData());
                doHeartBeatTask(ctx);
            }
        }, 10, TimeUnit.SECONDS);
    }

    private RpcProtocol<Void> buildHeartBeatData() {
        RpcProtocol<Void> protocol = new RpcProtocol<>();       // 构造一个心跳类型的请求

        MsgHeader header = new MsgHeader();
        long requestId = RpcRequestHolder.REQUEST_ID_GEN.incrementAndGet();
        header.setMagic(ProtocolConstants.MAGIC);
        header.setVersion(ProtocolConstants.VERSION);
        header.setRequestId(requestId);
        header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
        header.setMsgType((byte) MsgType.HEARTBEAT.getType());
        header.setStatus(HeartBeatData.STATUS_OK);                  // 心跳状态

        protocol.setHeader(header);                                 // 心跳只要头，无需体

        return protocol;
    }

}