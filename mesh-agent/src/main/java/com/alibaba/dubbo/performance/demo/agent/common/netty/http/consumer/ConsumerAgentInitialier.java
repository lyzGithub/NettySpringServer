package com.alibaba.dubbo.performance.demo.agent.common.netty.http.consumer;

import com.alibaba.dubbo.performance.demo.agent.common.netty.http.info.Order;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonRequestEncoder;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonResponseDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

public class ConsumerAgentInitialier extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("http-decoder",
                new HttpResponseDecoder());
        pipeline.addLast("http-aggregator",
                new HttpObjectAggregator(65536));
        // json解码器
        pipeline.addLast("json-decoder", new HttpJsonResponseDecoder(Order.class, true));
        pipeline.addLast("http-encoder",
                new HttpRequestEncoder());
        pipeline.addLast("json-encoder",
                new HttpJsonRequestEncoder());
        pipeline.addLast("jsonClientHandler",
                new ConsumerAgentHandler());
    }
}
