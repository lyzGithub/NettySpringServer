package com.alibaba.dubbo.performance.demo.agent.common.netty.http.provider;

import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.AgentRequest;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.AgentResponse;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.AgentRpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.ObjectAndByte;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonRequest;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.jsonCode.HttpJsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ProviderHandler extends SimpleChannelInboundHandler<HttpJsonRequest> {
    private Logger logger = LoggerFactory.getLogger(ProviderHandler.class);

    private RpcClient rpcClient;
    IRegistry registry;
    public ProviderHandler(IRegistry registry){
        this.registry = registry;
        this.rpcClient = new RpcClient(registry);
    }
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, HttpJsonRequest msg) throws Exception {
        logger.info("Get one massege in the provider!");
        HttpRequest request = msg.getRequest();
        AgentRequest agentRequest = (AgentRequest) msg.getBody();
        AgentRpcInvocation agentRpcInvocation = (AgentRpcInvocation)agentRequest.getData();
        logger.info("message is: " + agentRpcInvocation.getAttachment("path")+" "+agentRequest.getMethodName()+" "+agentRequest.getParameterTypesString()+" "+
                new String(agentRpcInvocation.getArguments()));

        //System.out.println("Http server receive request : " + order);
        Object result = rpcClient.invoke(agentRpcInvocation.getAttachment("path"),agentRequest.getMethodName(),agentRequest.getParameterTypesString(),
                new String(agentRpcInvocation.getArguments()));

        byte [] myB = ObjectAndByte.toByteArray(result);
        AgentResponse agentResponse = new AgentResponse();
        agentResponse.setBytes((byte[]) result );
        ChannelFuture future = ctx.writeAndFlush(new HttpJsonResponse(null, agentResponse));
        if (!HttpUtil.isKeepAlive(request)) {
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                public void operationComplete(Future future) throws Exception {
                    ctx.close();
                }
            });
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("失败: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
