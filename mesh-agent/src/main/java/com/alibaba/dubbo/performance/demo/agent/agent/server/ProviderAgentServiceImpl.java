package com.alibaba.dubbo.performance.demo.agent.agent.server;

@RpcService(ProviderAgentServiceImpl.class)

public class ProviderAgentServiceImpl implements ProviderAgentService{
    public ProviderAgentServiceImpl(){}

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

}
