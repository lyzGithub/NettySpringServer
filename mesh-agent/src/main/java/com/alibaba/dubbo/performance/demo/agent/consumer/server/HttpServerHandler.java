package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.agent.client.ConsumerAgentRpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.client.RPCFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.client.proxy.IAsyncObjectProxy;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentService;
import com.alibaba.dubbo.performance.demo.agent.consumer.server.common.FastJsonUtils;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
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
import java.util.concurrent.TimeUnit;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;

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
    private  ConsumerAgentRpcClient rpcClient;
    public HttpServerHandler(RegisteGetThread registeGetThread){
        this.registeGetThread = registeGetThread;
    }
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

        /*System.out.println("Http server receive request  " + fullHttpRequest);
        System.out.println("VERSION: " + fullHttpRequest.getProtocolVersion().text() + "\r\n");
        System.out.println("REQUEST_URI: " + fullHttpRequest.getUri() + "\r\n\r\n");
        System.out.println("\r\n\r\n");
        for (Map.Entry<String, String> entry : fullHttpRequest.headers()) {
            System.out.println("HEADER: " + entry.getKey() + '=' + entry.getValue() + "\r\n");
        }
*/

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1 , OK);
        // 设置缓存大小
//          ByteBuffer byteBuffer = new ByteBuffer();
//          byteBuffer.size();
//          byteBuffer.append("恭喜你,成功了!");

        HttpMethod method = fullHttpRequest.method();
        String hashCode = "";
        Map<String, String> paraMap = null;
        if (HttpMethod.GET == method) {
            // 是GET请求
        } else if (HttpMethod.POST == method) { // 是POST请求
            paraMap = getParaMap(fullHttpRequest);
            hashCode = Integer.toString(paraMap.get("parameter").hashCode());
        } else {
            // 不支持其它方法
            try {
                throw new Exception("除[GET|POST]外，不支持其它方法!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        Endpoint endpoint = registeGetThread.getEndPoint();

        String hostIp = endpoint.getHost();
        int port = endpoint.getPort();
        System.out.println("in consumer, provider url:" + hostIp+":"+port);
        ConsumerAgentRpcClient consumerAgentRpcClient = new ConsumerAgentRpcClient(hostIp, port);
        rpcClient = new ConsumerAgentRpcClient(hostIp,port);
        IAsyncObjectProxy client = rpcClient.createAsync(ProviderAgentService.class);
        RPCFuture helloFuture = client.call("hello", paraMap.get("parameter"));
        String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
        System.out.println(result);



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
