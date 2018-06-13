package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.agent.RequestPara;
import com.alibaba.dubbo.performance.demo.agent.agent.client.ConsumerAgentRpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.client.RPCFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.client.proxy.IAsyncObjectProxy;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentService;

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


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
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
    private RegisteGetThread registeGetThread;
    private  ConsumerAgentRpcClient rpcClient = new ConsumerAgentRpcClient();
    ;
    private static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    public HttpServerHandler(RegisteGetThread registeGetThread){
        this.registeGetThread = registeGetThread;
    }
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final FullHttpRequest fullHttpRequest) throws Exception {

        HttpConsumerServer.submit(new Runnable() {
            @Override
            public void run() {
                handleRequestDirectReturnTest(ctx, fullHttpRequest);
            }
        });


    }

    private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest){

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


        IAsyncObjectProxy client = rpcClient.createAsync(ProviderAgentService.class);

        RPCFuture helloFuture = client.call("getHashCode",
                new RequestPara(paraMap.get("interface"),paraMap.get("method"),paraMap.get("parameterTypesString"),
                        paraMap.get("parameter")));

        //RPCFuture helloFuture = client.call("hello", paraMap.get("parameter"));

        String result = null;
        try {
            result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        hashCode = result;


        byte[] hashBytes = hashCode.getBytes();
        httpResponse.content().writeBytes(hashBytes);
        httpResponse.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        httpResponse.headers().setInt( CONTENT_LENGTH, httpResponse.content().writerIndex());

        //Thread.sleep(1000);

        ChannelFuture future = ctx.writeAndFlush(httpResponse);

        if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                public void operationComplete(Future future) throws Exception {
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
            future.addListener(new GenericFutureListener<Future<? super Void>>() {
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
