package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;

/**
 * Created by carl.yu on 2016/12/16.
 */
public class ConsumerHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private int count = 0;
    private static Logger logger = LoggerFactory.getLogger(ConsumerHttpServerHandler.class);
    //private static ThreadPoolExecutor threadPoolExecutor;
    private static AsyncHttpClient asyncHttpClient;
    private static final ExecutorService executor = new ThreadPoolExecutor(50, 50, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100000));//CPU核数4-10倍

    private static Object lock = new Object();
    private static RegisteGetThread registeGetThread;
    public ConsumerHttpServerHandler(RegisteGetThread registeGetThread, AsyncHttpClient asyncHttpClient){
        this.asyncHttpClient = asyncHttpClient;
        this.registeGetThread = registeGetThread;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {
        //handleRequest(ctx,fullHttpRequest);
        /*RunTread runTread = new RunTread(ctx.channel(),fullHttpRequest);
        Thread thread = new Thread(runTread);
        thread.run();*/
        doBusiness(ctx,fullHttpRequest);
    }
    private void doBusiness(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        //异步线程池处理
        executor.submit( () -> {
            handleRequestDirectReturnTest(ctx,fullHttpRequest);
        });
    }

    private class RunTread implements Runnable{
        private ChannelHandlerContext ch;
        private  FullHttpRequest fullHttpRequest;
        public RunTread(ChannelHandlerContext ch, FullHttpRequest fullHttpRequest){
            this.ch = ch;
            this.fullHttpRequest = fullHttpRequest;
        }
        @Override
        public void run() {
            handleRequestDirectReturnTest(ch,fullHttpRequest);
            //handleRequest(ch,fullHttpRequest);
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

    private void sendError(ChannelHandlerContext ctx,
                                  HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                status, Unpooled.copiedBuffer("失败: " + status.toString()
                + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


    private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){


        HttpMethod method = fullHttpRequest.method();
        //String hashCode = "";
        Map<String, String> paraMap = null;
        if (HttpMethod.POST == method) {
            // 是POST请求
            paraMap = getParaMap(fullHttpRequest);
        } else if (HttpMethod.GET == method) {
            // 是GET请求
            try {
                throw new Exception("除[POST]外，不支持其它方法!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 不支持其它方法
            try {
                throw new Exception("除[POST]外，不支持其它方法!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Endpoint endpoint = registeGetThread.getEndPoint();
        String url = "http://"+endpoint.getHost()+":"+endpoint.getPort();

        org.asynchttpclient.Request request = org.asynchttpclient.Dsl.post(url)
                .addFormParam("interface", paraMap.get("interface"))
                .addFormParam("method", paraMap.get("method"))
                .addFormParam("parameterTypesString", paraMap.get("parameterTypesString"))
                .addFormParam("parameter", paraMap.get("parameter"))
                .build();

        ListenableFuture<Response> responseFuture = asyncHttpClient.executeRequest(request);


        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1 , OK);
        String hashCode = null;
        try {
            hashCode = responseFuture.get().getResponseBody();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        httpResponse.content().writeBytes(hashCode.getBytes());
        //System.out.println("hashCode: "+hashCode);


        httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        httpResponse.headers().setInt( CONTENT_LENGTH, httpResponse.content().writerIndex());

        //logger.info("return httpResponse");

        ChannelFuture future = ctx.writeAndFlush(httpResponse);

        if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
            future.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
                public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                    ctx.close();
                }
            });
        }
    }

    private  void handleRequestDirectReturnTest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1 , OK);

        HttpMethod method = fullHttpRequest.method();
        String hashCode = "";
        Map<String, String> paraMap = null;
        if (HttpMethod.POST == method) {
            // 是POST请求
            paraMap = getParaMap(fullHttpRequest);
        } else if (HttpMethod.GET == method) {
            // 是GET请求
            try {
                throw new Exception("除[POST]外，不支持其它方法!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // 不支持其它方法
            try {
                throw new Exception("除[POST]外，不支持其它方法!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        hashCode = Integer.toString(paraMap.get("parameter").hashCode());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] hashBytes = hashCode.getBytes();
        httpResponse.content().writeBytes(hashBytes);
        httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        httpResponse.headers().setInt( CONTENT_LENGTH, httpResponse.content().writerIndex());

        ChannelFuture future = ctx.writeAndFlush(httpResponse);

        if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
            future.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
                public void operationComplete(Future future) throws Exception {
                    ctx.close();
                }
            });
        }
    }


    public static Map<String, String> getParaMap(FullHttpRequest request){
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        decoder.offer(request);
        List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
        Map<String, String> parmMap = new HashMap<>();
        for (InterfaceHttpData parm : parmList) {
            Attribute data = (Attribute) parm;
            try {
                parmMap.put(data.getName(), data.getValue());
                //System.out.println("para: "+data.getName()+": "+data.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parmMap;
    }


}
