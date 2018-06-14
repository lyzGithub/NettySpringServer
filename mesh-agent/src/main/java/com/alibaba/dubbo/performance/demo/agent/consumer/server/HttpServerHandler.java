package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
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
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private int count = 0;
    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    //private static ThreadPoolExecutor threadPoolExecutor;
    private AsyncHttpClient asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient();

    private static Object lock = new Object();
    RegisteGetThread registeGetThread;
    public HttpServerHandler(RegisteGetThread registeGetThread){
        this.registeGetThread = registeGetThread;
    }


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {

        //HttpRequestHolderThread.dealRequest(ctx,fullHttpRequest,registeGetThread);
        handleRequest(ctx,fullHttpRequest);
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
        System.out.println("hashCode: "+hashCode);


        /*Runnable callback = () -> {
            try {
                // 检查返回值是否正确,如果不正确返回500。有以下原因可能导致返回值不对:
                // 1. agent解析dubbo返回数据不对
                // 2. agent没有把request和dubbo的response对应起来
                try {
                    String hashCode = responseFuture.get().getResponseBody();
                    httpResponse.content().writeBytes(hashCode.getBytes());
                    System.out.println("hashCode: "+hashCode);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        responseFuture.addListener(callback, null);*/

        httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        httpResponse.headers().setInt( CONTENT_LENGTH, httpResponse.content().writerIndex());

        logger.info("return httpResponse");

        ChannelFuture future = ctx.writeAndFlush(httpResponse);

        if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
            future.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
                public void operationComplete(io.netty.util.concurrent.Future future) throws Exception {
                    ctx.close();
                }
            });
        }
    }

    private void handleRequestDirectReturnTest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){

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


    private Map<String, String> getParaMap(FullHttpRequest request){
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
