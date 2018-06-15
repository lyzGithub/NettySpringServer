package com.alibaba.dubbo.performance.demo.agent.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.MyStringResponse;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyClientFuture;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyRequestHolder;
import com.alibaba.dubbo.performance.demo.agent.consumer.server.ConsumerHttpServerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClientHandler  extends SimpleChannelInboundHandler<MyStringResponse> {
    private static Logger logger = LoggerFactory.getLogger(ConsumerHttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyStringResponse response) {
        String requestId = response.getRequestId();
        long startM1 = System.currentTimeMillis();
        NettyClientFuture future = NettyRequestHolder.get(requestId);
        long endM1 = System.currentTimeMillis();
        logger.info("get future spend time: " + (endM1 - startM1));
        if(null != future){
            long startM2 = System.currentTimeMillis();
            NettyRequestHolder.remove(requestId);
            future.done(response);
            long endM2 = System.currentTimeMillis();
            logger.info("remove future spend time: " + (endM2 - startM2));
        }
    }
}
