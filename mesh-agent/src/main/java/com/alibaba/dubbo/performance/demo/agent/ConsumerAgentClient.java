package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.*;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.consumer.ConsumerAgentConnectManager;

import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ConsumerAgentClient {
    private Logger logger = LoggerFactory.getLogger(ConsumerAgentClient.class);

    private ConsumerAgentConnectManager connectManager;

    public ConsumerAgentClient(IRegistry registry){
        this.connectManager = new ConsumerAgentConnectManager();
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter,final String hostName, final int port) throws Exception {

        Channel channel = connectManager.getChannel(hostName,port);

        AgentRpcInvocation invocation = new AgentRpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        AgentJsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        AgentRequest request = new AgentRequest();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        logger.info("requestId=" + request.getId());

        AgentRpcFuture future = new AgentRpcFuture();
        AgentRpcRequestHolder.put(String.valueOf(request.getId()),future);

        channel.writeAndFlush(request);

        Object result = null;
        try {
            result = future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
