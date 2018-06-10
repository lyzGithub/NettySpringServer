package com.alibaba.dubbo.performance.demo.agent.consumer.server;

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
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class HttpConsumerServer {
    public void run(final String host,  final int port) throws Exception {
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap=new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                            socketChannel.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                            socketChannel.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                            socketChannel.pipeline().addLast("ServerHandler",new HttpServerHandler());

                        }
                    });
            System.out.println("服务器网址:"+host+":"+port);
            ChannelFuture future = bootstrap.bind(host,port).sync();
            System.out.println("服务器已启动>>网址:"+host+":"+port);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String host, int port) throws Exception {
        new HttpConsumerServer().run(host,port);
    }
}