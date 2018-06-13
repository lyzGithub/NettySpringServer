package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.protocol.RpcDecoder;
import com.alibaba.dubbo.performance.demo.agent.agent.protocol.RpcEncoder;
import com.alibaba.dubbo.performance.demo.agent.agent.protocol.RpcRequest;
import com.alibaba.dubbo.performance.demo.agent.agent.protocol.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.dubbo.TestRPCClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import org.apache.commons.collections4.MapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RPC Server
 *
 * @author huangyong, luxiaoxun
 */
public class ProviderAgentRpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ProviderAgentRpcServer.class);

    private String hostIp;
    private int port;
    private IRegistry registry;

    private Map<String, Object> handlerMap = new HashMap<>();
    private static ThreadPoolExecutor threadPoolExecutor;

    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;


    public ProviderAgentRpcServer(String hostIp, int port) {
        this.hostIp = hostIp;
        this.port = port;
        registry = new EtcdRegistry(System.getProperty("etcd.url"));
        StartNettyThread startNettyThread = new StartNettyThread();
        Thread thread = new Thread(startNettyThread);
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                logger.info("Loading service: {}", interfaceName);
                handlerMap.put(interfaceName, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        StartNettyThread startNettyThread = new StartNettyThread();
        Thread thread = new Thread(startNettyThread);
        thread.start();
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public static void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (ProviderAgentRpcServer.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(16, 64, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
        logger.info("provider Server now active thread is: "+threadPoolExecutor.getActiveCount());
    }

    public ProviderAgentRpcServer addService(String interfaceName, Object serviceBean) {
        if (!handlerMap.containsKey(interfaceName)) {
            logger.info("Loading service: {}", interfaceName);
            handlerMap.put(interfaceName, serviceBean);
        }
        return this;
    }

    private class StartNettyThread implements Runnable{
        @Override
        public void run() {
            try {
                start();
            } catch (Exception ex) {
                logger.error("Exception: {}", ex);
            }
        }
        private void start() throws Exception {
            logger.info("In ProviderAgentRpcServer start");
            if (bossGroup == null && workerGroup == null) {
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                        .addLast(new RpcDecoder(RpcRequest.class))
                                        .addLast(new RpcEncoder(RpcResponse.class))
                                        .addLast(new RpcHandler(handlerMap));
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                ChannelFuture future = bootstrap.bind(hostIp, port).sync();
                logger.info("Server started on port {}", port);
                future.channel().closeFuture().sync();
            }
    }


    }

    public static void run(String hostIp, int port){
        ProviderAgentRpcServer providerAgentRpcServer = new ProviderAgentRpcServer(hostIp,port);

    }
}
