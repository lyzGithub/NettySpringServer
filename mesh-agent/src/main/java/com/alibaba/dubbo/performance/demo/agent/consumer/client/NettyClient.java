package com.alibaba.dubbo.performance.demo.agent.consumer.client;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.MyStringRequest;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyClientFuture;
import com.alibaba.dubbo.performance.demo.agent.consumer.client.model.NettyRequestHolder;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyClient {
    private Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private NettyClientConnecManager connectManager;

    public NettyClient(String host, int port){
        this.connectManager = new NettyClientConnecManager(host, port);
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        Channel channel = connectManager.getChannel();

        MyStringRequest myStringRequest = new MyStringRequest(interfaceName,method,parameterTypesString,
                parameter);
        NettyClientFuture future = new NettyClientFuture();
        NettyRequestHolder.put(myStringRequest.getId(),future);

        channel.writeAndFlush(myStringRequest);

        Object result = null;
        try {
            result = future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        //logger.info("return requestId=" + request.getId() + new String((byte[])result));

        return result;
    }
}
