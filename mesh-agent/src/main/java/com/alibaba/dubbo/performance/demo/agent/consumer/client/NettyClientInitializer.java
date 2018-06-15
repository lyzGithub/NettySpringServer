package com.alibaba.dubbo.performance.demo.agent.consumer.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new ObjectDecoder(2048, ClassResolvers.cacheDisabled(this
                        .getClass().getClassLoader())));
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new NettyClientHandler());
    }
}