package org.lmmarise.rpc.cusumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.codec.RpcDecoder;
import org.lmmarise.rpc.codec.RpcEncoder;
import org.lmmarise.rpc.common.RpcRequest;
import org.lmmarise.rpc.common.ServiceMeta;
import org.lmmarise.rpc.handler.RpcHeartBeatHandler;
import org.lmmarise.rpc.handler.RpcResponseHandler;
import org.lmmarise.rpc.protocol.RpcProtocol;
import org.lmmarise.rpc.provider.registry.RegistryService;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lmmarise.rpc.common.RpcServiceHelper.*;

/**
 * RPC 客户端，负责发起远程调用，请求远程服务并获取响应报文。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/24 18:51
 */
@Slf4j
public class RpcConsumer {
    private final static Bootstrap bootstrap = new Bootstrap();
    private final static EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    public static ChannelPoolMap<String, FixedChannelPool> poolMap;  // 缓存客户端与服务端建立的连接，key："address#port"

    public static HashSet cache = new HashSet();
    public static AtomicInteger count = new AtomicInteger(0);

    static {
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
    }

    /**
     * 启动本地 Netty 服务。
     */
    public RpcConsumer() {
        initChannelPool();
    }

    public void initChannelPool() {
        poolMap = new AbstractChannelPoolMap<String, FixedChannelPool>() {
            @Override
            protected FixedChannelPool newPool(String addressPortKey) {
                AbstractChannelPoolHandler handler = new AbstractChannelPoolHandler() {
                    @Override
                    public void channelReleased(Channel ch) {
                        ch.writeAndFlush(Unpooled.EMPTY_BUFFER);                    // flush 掉所有写回的数据
                    }

                    @Override
                    public void channelCreated(Channel ch) {
                        ch.pipeline()
                                .addLast(new RpcEncoder())
                                .addLast(new RpcDecoder())
                                .addLast(new RpcHeartBeatHandler())     // 向服务端发送心跳
                                .addLast(new RpcResponseHandler());
                    }
                };
                String[] strings = deBuildAddressPortKey(addressPortKey);
                bootstrap.remoteAddress(strings[0], Integer.parseInt(strings[1]));
                return new FixedChannelPool(bootstrap, handler, 5); // maxConnections 单个 host 连接池大小
            }
        };
    }

    /**
     * 本地负载均衡后并发出 RPC 请求。
     */
    public void sendRequest(RpcProtocol<RpcRequest> protocol, RegistryService registryService) throws Exception {
        // 提取请求信息
        RpcRequest request = protocol.getBody();
        Object[] params = request.getParams();

        String serviceKey = buildServiceKey(request.getClassName(), request.getServiceVersion());
        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();

        ServiceMeta serviceMetadata = registryService.discovery(serviceKey, invokerHashCode);   // 负债均衡，选择一个服务节点

        if (serviceMetadata != null) {      // 对该服务节点发起 RPC 请求
            String address = serviceMetadata.getServiceAddress();
            int port = serviceMetadata.getServicePort();

            final SimpleChannelPool channelPool = poolMap.get(buildAddressPortKey(address, port)); // 与目标主机服务的连接池

            Future<Channel> future = channelPool.acquire();             // 从连接池取出一个连接进行服务

            future.addListener((FutureListener<Channel>) futureListener -> {
                if (futureListener.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort());
                    Channel channel = futureListener.getNow();
                    channel.writeAndFlush(protocol).sync();
                    channelPool.release(channel);
                } else {
                    log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddress(), serviceMetadata.getServicePort());
                    futureListener.cause().printStackTrace();
                }
            });
        }
    }
}
