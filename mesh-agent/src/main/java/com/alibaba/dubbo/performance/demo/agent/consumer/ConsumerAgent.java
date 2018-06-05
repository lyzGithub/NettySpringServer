package com.alibaba.dubbo.performance.demo.agent.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.info.Order;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonRequestEncoder;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonResponseDecoder;

import java.net.InetSocketAddress;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ConsumerAgent {
    public void connect(final String hostName, final int port, Object body) throws Exception {
        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast("http-decoder",
                                    new HttpResponseDecoder());
                            ch.pipeline().addLast("http-aggregator",
                                    new HttpObjectAggregator(65536));
                            // json解码器
                            ch.pipeline().addLast("json-decoder", new HttpJsonResponseDecoder(Order.class, true));
                            ch.pipeline().addLast("http-encoder",
                                    new HttpRequestEncoder());
                            ch.pipeline().addLast("json-encoder",
                                    new HttpJsonRequestEncoder());
                            ch.pipeline().addLast("jsonClientHandler",
                                    new ConsumerHandler());
                        }
                    });

            // 发起异步连接操作
            ChannelFuture f = b.connect(new InetSocketAddress(hostName, port)).sync();

            // 当代客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    /**
     * @params hostName, port
     * @throws Exception
     */
    public static void run(final String hostName, final int port, Object body) throws Exception {

        new ConsumerAgent().connect(hostName, port, body);
    }
}