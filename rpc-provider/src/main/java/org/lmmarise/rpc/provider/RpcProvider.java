package org.lmmarise.rpc.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.lmmarise.rpc.codec.RpcDecoder;
import org.lmmarise.rpc.codec.RpcEncoder;
import org.lmmarise.rpc.common.RpcServiceHelper;
import org.lmmarise.rpc.common.ServiceMeta;
import org.lmmarise.rpc.handler.RpcHeartBeatHandler;
import org.lmmarise.rpc.handler.RpcIdleStateHandler;
import org.lmmarise.rpc.handler.RpcRequestHandler;
import org.lmmarise.rpc.provider.annotation.RpcService;
import org.lmmarise.rpc.provider.registry.RegistryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * RpcService 注解处理器，依赖于 Spring IoC 实现，负责注册服务、本地暴露。
 *
 * @author lmmarise.j@gmail.com
 * @since 2022/5/23 18:45
 */
@Slf4j
public class RpcProvider implements InitializingBean, BeanPostProcessor {

    private String serverAddress;
    private final int serverPort;
    private final RegistryService serviceRegistry;

    private final Map<String, Object> rpcServiceMap = new HashMap<>();

    public RpcProvider(int serverPort, RegistryService serviceRegistry) {
        this.serverPort = serverPort;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * bean 实例回调。
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            try {
                startRpcServer();
            } catch (Exception e) {
                log.error("start rpc server error.", e);
            }
        }).start();
    }

    /**
     * 服务端启动。配置线程池、Channel 初始化、端口绑定、对外暴露本地服务。
     */
    private void startRpcServer() throws UnknownHostException, InterruptedException {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(new RpcIdleStateHandler())     // 连接活跃检测
                                    .addLast(new RpcEncoder())      // RPC 协议报文编码器
                                    .addLast(new RpcDecoder())      // RPC 协议报文解码器
                                    .addLast(new RpcHeartBeatHandler())     // 心跳处理
                                    .addLast(new RpcRequestHandler(rpcServiceMap));     // RPC 请求处理器
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.bind(this.serverAddress, this.serverPort).sync();
            log.info("server address {} started on port {}", this.serverAddress, this.serverPort);
            channelFuture.channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    /**
     * 收集需要暴露服务的 bean，将服务、本地暴露接口等信息注册到注册中心，在 rpcServiceMap 中维护被 @RpcService 注解的 bean 引用。
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        if (rpcService != null) {
            String serviceName = rpcService.serviceInterface().getName();
            String serviceVersion = rpcService.serviceVersion();

            try {
                ServiceMeta serviceMeta = new ServiceMeta();
                serviceMeta.setServiceAddress(serverAddress);
                serviceMeta.setServicePort(serverPort);
                serviceMeta.setServiceName(serviceName);
                serviceMeta.setServiceVersion(serviceVersion);

                serviceRegistry.register(serviceMeta);      // 将接口实现版本信息暴露到注册中心，不直接暴露本地实现信息，降低耦合与复杂度

                // Zookeeper 中 serviceMeta key 与本地 IoC 容器中 bean 对应，方便客户端请求时根据 key 调用 bean 方法提供服务

                // 将服务暴露注解上 serviceInterface 指定的接口全限类名+版本号，与本地 IoC 容器中的 Bean 做映射，例如：
                // org.lmmarise.rpc.facade.HelloFacade#1.0.0       =>       HelloFacadeImpl@4634
                rpcServiceMap.put(
                        RpcServiceHelper.buildServiceKey(serviceMeta.getServiceName(), serviceMeta.getServiceVersion()),
                        bean
                );
            } catch (Exception e) {
                log.error("failed to register service {}#{}", serviceName, serviceVersion, e);
            }
        }
        return bean;
    }
}
