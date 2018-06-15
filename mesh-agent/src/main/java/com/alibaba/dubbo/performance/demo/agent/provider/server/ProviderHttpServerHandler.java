package com.alibaba.dubbo.performance.demo.agent.provider.server;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.MyStringRequest;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.MyStringResponse;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ProviderHttpServerHandler extends SimpleChannelInboundHandler<MyStringRequest> {
    private int count = 0;
    private static Logger logger = LoggerFactory.getLogger(ProviderHttpServerHandler.class);
    private RpcClient rpcClient;
    public ProviderHttpServerHandler(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final MyStringRequest request) throws Exception {

        //long startM = System.currentTimeMillis();
        handleRequest(ctx,request);
        //handleRequestDirectReturnTest(ctx,fullHttpRequest);
        //long endM = System.currentTimeMillis();
        //logger.info("spend time: " + (endM - startM));
        /*RunTread runTread = new RunTread(ctx, fullHttpRequest);
        Thread thread = new Thread(runTread);
        thread.start();*/
    }

    private class RunTread implements Runnable{
        private ChannelHandlerContext ctx;
        private  MyStringRequest request;
        public RunTread(ChannelHandlerContext ctx, MyStringRequest request){
            this.ctx = ctx;
            this.request = request;
        }
        @Override
        public void run() {
            handleRequest(ctx,request);
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, MyStringRequest request){


        byte[] result = null;

        try {
            result = (byte[])rpcClient.invoke(request.getInterfaceName(),request.getMethodName(),
                    request.getParameterTypesString(), request.getParameter());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MyStringResponse response = new MyStringResponse(request.getId(),result);

        //Thread.sleep(1000);

        ChannelFuture future = ctx.writeAndFlush(response);

    }

    private void handleRequestDirectReturnTest(ChannelHandlerContext ctx, MyStringRequest request){


        String hashCode = Integer.toString(request.getParameter().hashCode());

        byte[] hashBytes = hashCode.getBytes();
        MyStringResponse response = new MyStringResponse(request.getId(),hashBytes);
        ChannelFuture future = ctx.writeAndFlush(response);

    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, -1);
        }
    }

    private static void sendError(ChannelHandlerContext ctx,
                                  int status) {
        MyStringResponse response = new MyStringResponse(Integer.toString(status),Integer.toString(status).getBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
