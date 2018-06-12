package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.RequestPara;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.provider.client.ClientToProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcService(ProviderAgentServiceImpl.class)

public class ProviderAgentServiceImpl implements ProviderAgentService{
    public ProviderAgentServiceImpl(){
        System.out.println("hh "+this.getClass().toString());
    }
    private RpcClient rpcClient = new RpcClient();
    private static Logger logger = LoggerFactory.getLogger(ProviderAgentServiceImpl.class);

    @Override
    public String hello(String para) {
        logger.info("receive: " + para);
        return "hello " + para;
    }

    @Override
    public String getHashCode(RequestPara requestPara) {
        ClientToProvider.run();
        Object result = null;
        try {
            result = rpcClient.invoke(requestPara.getInterfaceName(),requestPara.getMethodName(),requestPara.getParameterTypesString()
            ,requestPara.getParameter());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String hashCode = new String((byte[]) result);
        logger.info(hashCode);
        return hashCode;
    }

}
