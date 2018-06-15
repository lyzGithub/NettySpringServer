package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.CONTENT_LENGTH;

public class HttpRequestHolderThread {
    private static Logger logger = LoggerFactory.getLogger(HttpRequestHolderThread.class);
    private static AsyncHttpClient asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient();

    private ChannelHandlerContext ctx;
    private FullHttpRequest fullHttpRequest;


    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    public static void dealRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, RegisteGetThread registeGetThread) {
        //异步线程池处理
        threadPoolExecutor.submit( () -> {
            long startM = System.currentTimeMillis();
            handleRequest(ctx,fullHttpRequest,registeGetThread);
            //handleRequestDirectReturnTest(ctx,fullHttpRequest);
            long endM = System.currentTimeMillis();
            logger.info("spend time: " + (endM - startM));
        });
    }

    public HttpRequestHolderThread(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest,RegisteGetThread registeGetThread){

    }

    private static void handleRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest,RegisteGetThread registeGetThread){
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

    private static  void handleRequestDirectReturnTest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){

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


    private static Map<String, String> getParaMap(FullHttpRequest request){
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