package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderAgentServerHandler extends ChannelInboundHandlerAdapter {
    private int counter;
    private Logger logger = LoggerFactory.getLogger(ProviderAgentServerHandler.class);
    private RpcClient rpcClient;

    public ProviderAgentServerHandler(IRegistry registry){
        rpcClient = new RpcClient(registry);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        String body = (String) msg;
        logger.info("The time server receive order : " + body
                + " ; the counter is : " + ++counter);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                System.currentTimeMillis()).toString() : "BAD ORDER";
        currentTime = currentTime + System.getProperty("line.separator");
        String interFaceName = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
        String method = "hash";
        String path = "Ljava/lang/String;";
        String para = currentTime;
        byte[] bytes = (byte[])rpcClient.invoke(interFaceName,method,path,para);
        String hashCode = new String(bytes);
        String all = hashCode + "+ "+ currentTime;
        logger.info("hashcode: " + hashCode);
        ByteBuf resp = Unpooled.copiedBuffer(all.getBytes());
        ctx.writeAndFlush(resp);
        logger.info("write done!!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

}
