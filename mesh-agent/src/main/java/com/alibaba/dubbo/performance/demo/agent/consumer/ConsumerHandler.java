package com.alibaba.dubbo.performance.demo.agent.consumer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.info.Order;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonRequest;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ConsumerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接上服务器...");
        Order order = new Order();
        //System.out.println("oder: "+order.getCustomer().toString());
        HttpJsonRequest request = new HttpJsonRequest(null, new Order());
        System.out.println("Request json done!!!");
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(msg.getClass().getName());
        System.out.println("接收到了数据..." + msg);
    }

    /*@Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpJsonResponse msg) throws Exception {
        System.out.println("The client receive response of http header is : "
                + msg.getHttpResponse().headers().names());
        System.out.println("The client receive response of http body is : "
                + msg.getResult());
    }*/


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
