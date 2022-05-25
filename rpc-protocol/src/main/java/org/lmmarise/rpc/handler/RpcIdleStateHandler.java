package org.lmmarise.rpc.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.common.RpcConstants;

import java.util.concurrent.TimeUnit;

/**
 * 服务端对客户端连接空闲检测。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/25 15:03
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcIdleStateHandler extends IdleStateHandler {

    /**
     * 只关注读空闲时间，如果服务端 60s 未读到数据，就会回调 channelIdle() 方法。
     */
    public RpcIdleStateHandler() {
        super(RpcConstants.RPC_IDLE_OVERTIME, 0, 0, TimeUnit.SECONDS);
    }

    /**
     * 关闭该客户端连接。
     */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        log.info("连接超时：channel = {}", ctx.channel());
        ctx.channel().close();
    }
}