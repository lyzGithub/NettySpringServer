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

        long startM1 = System.currentTimeMillis();
        NettyRequestHolder.put(myStringRequest.getId(),future);
        long endM1 = System.currentTimeMillis();
        logger.info("put request spend time: " + (endM1 - startM1));


        long startM2 = System.currentTimeMillis();
        channel.writeAndFlush(myStringRequest);
        long endM2 = System.currentTimeMillis();
        logger.info("write flush request spend time: " + (endM2 - startM2));


        long startM3 = System.currentTimeMillis();
        Object result = null;
        try {
            result = future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        long endM3 = System.currentTimeMillis();
        logger.info("waiting response spend time: " + (endM3 - startM3));
        return result;
    }
}
