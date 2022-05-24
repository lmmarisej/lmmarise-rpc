package org.lmmarise.rpc.cusumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.codec.RpcDecoder;
import org.lmmarise.rpc.codec.RpcEncoder;
import org.lmmarise.rpc.common.RpcRequest;
import org.lmmarise.rpc.common.RpcServiceHelper;
import org.lmmarise.rpc.common.ServiceMeta;
import org.lmmarise.rpc.handler.RpcResponseHandler;
import org.lmmarise.rpc.protocol.RpcProtocol;
import org.lmmarise.rpc.provider.registry.RegistryService;

/**
 * RPC 客户端，负责发起远程调用，请求远程服务并获取处理结果。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 18:51
 */
@Slf4j
public class RpcConsumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    /**
     * 启动本地 Netty 服务。
     */
    public RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    /**
     * 本地负载均衡后并发出 RPC 请求。
     */
    public void sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        RpcRequest request = protocol.getBody();
        Object[] params = request.getParams();

        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();

        // 负债均衡，选择一个服务节点
        ServiceMeta serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);

        // 对该服务节点发起 RPC 请求
        if (serviceMetadata != null) {
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort()).sync();
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort());
                } else {
                    log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            future.channel().writeAndFlush(protocol);
        }
    }
}
