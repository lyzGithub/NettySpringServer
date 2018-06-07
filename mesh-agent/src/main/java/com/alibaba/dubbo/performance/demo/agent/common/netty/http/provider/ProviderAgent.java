package com.alibaba.dubbo.performance.demo.agent.common.netty.http.provider;

import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonRequestDecoder;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonResponseEncoder;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.net.InetSocketAddress;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ProviderAgent {
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    public void beginServe(final String hostName, final int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            //接收HttpJsonRequest，需要对应解码器
                            //ByteBuf->FullHttpRequest-> HttpJsonRequestDecoder
                            //输出HttpJsonResponse，需要对应编码器
                            //HttpResponseEncoder->FullHttpResponse-> HttpJsonResponseEncoder
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536));
                            ch.pipeline().addLast("json-decoder", new HttpJsonRequestDecoder(AgentRequest.class, true));
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            ch.pipeline().addLast("json-encoder", new HttpJsonResponseEncoder());
                            ch.pipeline().addLast("jsonServerHandler", new ProviderHandler(registry));
                        }
                    });
            ChannelFuture future = b.bind(new InetSocketAddress(hostName, port)).sync();
            System.out.println("网址是 : " + hostName+":"
                    + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void run(final String hostName, final int port) throws Exception {
        new ProviderAgent().beginServe(hostName, port);
    }
}
