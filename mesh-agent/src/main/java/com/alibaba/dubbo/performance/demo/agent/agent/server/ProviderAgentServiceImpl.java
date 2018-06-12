package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.RequestPara;
import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.dubbo.TestRPCClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RpcService(ProviderAgentServiceImpl.class)

public class ProviderAgentServiceImpl implements ProviderAgentService{

    private RpcClient rpcClient = new RpcClient();
    private static Logger logger = LoggerFactory.getLogger(ProviderAgentServiceImpl.class);

    public ProviderAgentServiceImpl(){
    }

    @Override
    public String hello(String para) {
        //logger.info("receive: " + para);
        return Integer.toString(para.hashCode());
    }

    @Override
    public String getHashCode(RequestPara requestPara) {

        /*Object result = null;
        try {
            result = rpcClient.invoke(requestPara.getInterfaceName(),requestPara.getMethodName(),
                    requestPara.getParameterTypesString(),requestPara.getParameter());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String hashCode = new String((byte[]) result);*/
        String hashCode = Integer.toString(requestPara.getParameter().hashCode());

        return hashCode;
    }

}
