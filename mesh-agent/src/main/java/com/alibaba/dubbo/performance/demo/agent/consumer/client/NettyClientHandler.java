package com.alibaba.dubbo.performance.demo.agent.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.MyStringResponse;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyClientFuture;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyRequestHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler  extends SimpleChannelInboundHandler<MyStringResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MyStringResponse response) {
        String requestId = response.getRequestId();
        NettyClientFuture future = NettyRequestHolder.get(requestId);
        if(null != future){
            NettyRequestHolder.remove(requestId);
            future.done(response);
        }
    }
}
