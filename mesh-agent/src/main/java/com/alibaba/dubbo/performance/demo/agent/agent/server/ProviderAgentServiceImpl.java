package com.alibaba.dubbo.performance.demo.agent.agent.server;

@RpcService(ProviderAgentServiceImpl.class)

public class ProviderAgentServiceImpl implements ProviderAgentService{
    public ProviderAgentServiceImpl(){
        System.out.println("hh "+this.getClass().toString());
    }

    @Override
    public String hello(String name) {
        System.out.println("receive: " + name);
        return "Hello! " + name;
    }

}
